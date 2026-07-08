package com.example.kotlintut.data.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaccia Retrofit per le chiamate API al ristorante
 */
interface ApiService {

    /** Recupera l'elenco completo delle categorie disponibili tramite una richiesta POST al server. */
    @POST("categorie")
    suspend fun getCategories(): ApiResponse<NetworkCategory>

    /** Invia una richiesta POST con filtri specifici per ottenere la lista dei prodotti dal server. */
    @POST("prodotti")
    suspend fun getProducts(@Body filter: Map<String, String>): ApiResponse<NetworkProduct>

    /** Invia un nuovo ordine al server. */
    @POST("ordine")
    suspend fun sendOrder(@Body payload: OrderPayload): retrofit2.Response<Unit>
}
