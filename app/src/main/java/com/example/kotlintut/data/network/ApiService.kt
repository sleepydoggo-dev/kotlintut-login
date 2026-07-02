package com.example.kotlintut.data.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaccia Retrofit per le chiamate API al ristorante
 */
interface ApiService {

    /**
     * Recupera tutte le categorie (chiamata POST come richiesto)
     */
    @POST("categorie")
    suspend fun getCategories(): ApiResponse<NetworkCategory>

    /**
     * Recupera i prodotti (chiamata POST filtrata per categoria nel body)
     */
    @POST("prodotti")
    suspend fun getProducts(@Body filter: Map<String, String>): ApiResponse<NetworkProduct>
}
