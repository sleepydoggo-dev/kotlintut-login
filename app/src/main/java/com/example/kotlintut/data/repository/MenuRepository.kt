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

    /**
     * Recupera le categorie: prima le locali, poi aggiorna dal server.
     */
    fun getCategories(): Flow<List<NetworkCategory>> = flow {
        // 1. Emetti dati locali subito
        val local = dbHelper.getAllCategoriesLocal()
        emit(local)

        try {
            // 2. Chiamata POST al server
            val response = api.getCategories()
            android.util.Log.d("MenuRepository", "API Response success: ${response.success}, data size: ${response.data.size}")
            if (response.success) {
                // 3. Upsert nel database
                dbHelper.upsertCategories(response.data)
                // 4. Emetti dati aggiornati
                val updated = dbHelper.getAllCategoriesLocal()
                android.util.Log.d("MenuRepository", "Local data after upsert size: ${updated.size}")
                emit(updated)
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "Error fetching categories", e)
            // In caso di errore (es. offline), abbiamo già emesso i dati locali
        }
    }

    /**
     * Recupera i prodotti di una categoria: prima locali, poi aggiorna dal server.
     */
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        // 1. Emetti locali subito
        emit(dbHelper.getProductsByCategory(categoryId))

        try {
            // 2. Chiamata POST con filtro categoria
            val response = api.getProducts(mapOf("categoria_id" to categoryId))
            if (response.success) {
                // 3. Upsert nel database
                dbHelper.upsertProducts(response.data, categoryId)
                // 4. Emetti dati aggiornati
                emit(dbHelper.getProductsByCategory(categoryId))
            }
        } catch (e: Exception) {
            // Offline o errore server
        }
    }
}
