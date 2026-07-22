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
    @POST("inserisciOrdine")
    suspend fun sendOrder(@Body payload: OrderPayload): retrofit2.Response<Unit>

    /** Registra un nuovo utente. */
    @POST("users")
    suspend fun registerUser(@Body request: RegistrationRequest): retrofit2.Response<RegistrationResponse>

    /** Effettua il login dell'utente. */
    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): retrofit2.Response<LoginResponse>

    /** Recupera lo storico degli ordini dell'utente. */
    @retrofit2.http.GET("ordini")
    suspend fun getOrders(): retrofit2.Response<List<NetworkOrder>>

    /** Crea un PaymentIntent su Stripe tramite il backend. */
    @POST("stripe")
    suspend fun createStripePayment(@Body payload: Map<String, String>): retrofit2.Response<StripeResponse>

    /** Annulla un pagamento Stripe. */
    @POST("stripeAnnullaPagamento")
    suspend fun cancelStripePayment(@Body payload: Map<String, String>): retrofit2.Response<Unit>

    /** Recupera i metodi di pagamento attivi. */
    @retrofit2.http.GET("pagamenti")
    suspend fun getPaymentMethods(): retrofit2.Response<List<NetworkPaymentMethod>>
}

data class NetworkPaymentMethod(
    @com.google.gson.annotations.SerializedName("_id") val id: String,
    @com.google.gson.annotations.SerializedName("nome") val nome: String,
    @com.google.gson.annotations.SerializedName("attivo") val attivo: Boolean
)

data class StripeResponse(
    @com.google.gson.annotations.SerializedName("status") val status: String,
    @com.google.gson.annotations.SerializedName("data") val data: com.google.gson.JsonObject
)
