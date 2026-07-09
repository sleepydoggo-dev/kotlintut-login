package com.example.kotlintut.data.repository

import com.example.kotlintut.data.db.DatabaseHelper
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.data.network.ApiService
import com.example.kotlintut.data.network.NetworkCategory
import com.example.kotlintut.data.network.NetworkProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository che gestisce la logica Offline-first per il menu.
 */
class MenuRepository(
    private val dbHelper: DatabaseHelper,
    private val api: ApiService
) {

    /** Recupera la lista delle categorie principali, emettendo prima i dati locali e poi aggiornandoli dal server. */
    fun getCategories(): Flow<List<NetworkCategory>> = flow {
        // 1. Emetti dati locali subito
        val local = dbHelper.getAllCategoriesLocal()
        emit(local)

        try {
            // Log del payload per debug/Postman (POST senza body)
            android.util.Log.d("MENU_API_LOG", "=== CATEGORIES REQUEST PAYLOAD ===\n{} (Empty Body)")

            // 2. Chiamata POST al server
            val response = api.getCategories()
            if (response.success) {
                val data = response.data ?: emptyList()
                android.util.Log.d("MenuRepository", "API Response success, data size: ${data.size}")
                
                // 3. Upsert nel database
                dbHelper.upsertCategories(data)
                
                // 4. Emetti dati aggiornati
                val updated = dbHelper.getAllCategoriesLocal()
                android.util.Log.d("MenuRepository", "Local data after upsert size: ${updated.size}")
                emit(updated)
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "Error fetching categories: ${e.message}", e)
        }
    }

    /** Recupera i prodotti di una specifica categoria, provando diverse chiavi di filtro API e aggiornando il database locale. */
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        // 1. Emetti locali subito
        val local = dbHelper.getProductsByCategory(categoryId)
        android.util.Log.d("MenuRepository", "Local products check for cat $categoryId: ${local.size}")
        emit(local)

        try {
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()

            // Tentativo 1: Chiave "categorie" (Plurale, standard per questa API)
            val filter1 = mapOf("categorie" to categoryId)
            android.util.Log.d("MENU_API_LOG", "=== PRODUCTS REQUEST (Attempt 1: 'categorie') ===\n${gson.toJson(filter1)}")
            var response = api.getProducts(filter1)
            
            // Tentativo 2: Fallback su "categoria" (Singolare)
            if (!response.success || response.data?.isEmpty() == true) {
                val filter2 = mapOf("categoria" to categoryId)
                android.util.Log.d("MENU_API_LOG", "=== PRODUCTS REQUEST (Attempt 2: 'categoria') ===\n${gson.toJson(filter2)}")
                response = api.getProducts(filter2)
            }

            // Tentativo 3: Fallback su "categoria_id"
            if (!response.success || response.data?.isEmpty() == true) {
                val filter3 = mapOf("categoria_id" to categoryId)
                android.util.Log.d("MENU_API_LOG", "=== PRODUCTS REQUEST (Attempt 3: 'categoria_id') ===\n${gson.toJson(filter3)}")
                response = api.getProducts(filter3)
            }

            if (response.success) {
                val data = response.data ?: emptyList()
                android.util.Log.d("MenuRepository", "API Response: SUCCESS. Products found: ${data.size}")
                
                // 3. Upsert nel database
                dbHelper.upsertProducts(data, categoryId)
                
                // 4. Emetti dati aggiornati
                val updated = dbHelper.getProductsByCategory(categoryId)
                android.util.Log.d("MenuRepository", "Emitting ${updated.size} products after update")
                emit(updated)
            } else {
                android.util.Log.e("MenuRepository", "API Response: FAILED or EMPTY. Success: ${response.success}")
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "NETWORK ERROR for $categoryId: ${e.message}", e)
        }
    }
}
