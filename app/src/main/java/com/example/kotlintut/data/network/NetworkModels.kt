package com.example.kotlintut.data.network

import com.google.gson.annotations.SerializedName

/**
 * Wrapper generico per le risposte dell'API
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<T>?,
)

/**
 * Modello categoria ricevuto dal server
 */
data class NetworkCategory(
    @SerializedName("_id") val id: String? = "",
    @SerializedName("categoriaPadre") val parentCategory: com.google.gson.JsonElement? = null,
    @SerializedName("posizionamento") val position: Int? = 0,
    @SerializedName("categoria") val name: String? = "",
    @SerializedName("visibile") val isVisible: Boolean? = true
)

/**
 * Modello ingrediente base
 */
data class NetworkIngredient(
    @SerializedName("_id") val internalId: String? = "",
    @SerializedName("idIngrediente") val id: String? = "",
    @SerializedName("nome") val name: String? = "",
    @SerializedName("eliminabile") val isRemovable: String? = "no", // "si" o "no"
    @SerializedName("qta") val qta: Int? = 1,
    @SerializedName("prezzo") val price: Double? = 0.0
)

/**
 * Modello aggiunta extra
 */
data class NetworkExtra(
    @SerializedName("_id") val internalId: String? = "",
    @SerializedName("idIngrediente") val id: String? = "",
    @SerializedName("nome") val name: String? = "",
    @SerializedName("prezzo") val price: Double? = 0.0,
    @SerializedName("qta") val qta: Int? = 1
)

/**
 * Modello opzione singola (formato o dimensione)
 */
data class NetworkOption(
    @SerializedName("valore") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("_id") val id: String = ""
)

/**
 * Modello attributo dinamico (es. FORMATO, DIMENSIONE)
 */
data class NetworkAttribute(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("valori") val values: List<NetworkOption>?
)

/**
 * Struttura specifica per gli attributi selezionati nell'invio dell'ordine
 */
data class OrderSelectedAttribute(
    @SerializedName("idAttributo") val attributeId: String,
    @SerializedName("nomeAttributo") val attributeName: String,
    @SerializedName("idValore") val valueId: String,
    @SerializedName("nome") val valueName: String,
    @SerializedName("prezzo") val price: Double
)

/**
 * Modello prodotto ricevuto dal server
 */
data class NetworkProduct(
    @SerializedName("_id") val id: String? = "",
    @SerializedName("nome") val name: String? = "",
    @SerializedName("prezzo") val price: Double? = 0.0,
    @SerializedName("categorie") val categories: List<String>? = emptyList(),
    @SerializedName("immagine") val imageUrl: String? = "",
    @SerializedName("disponibile") val isAvailable: Boolean? = true,
    @SerializedName("ingredienti") val ingredients: List<NetworkIngredient>? = emptyList(),
    @field:SerializedName("aggiunte") val extras: List<NetworkExtra>? = emptyList(),
    @field:SerializedName("attributi") val attributes: List<NetworkAttribute>? = emptyList(),
    @SerializedName("iva") val iva: NetworkIva? = null
)

// --- MODELLI PER AUTENTICAZIONE RESTIVUS ---

/** Dati del profilo utente */
data class RegistrationProfile(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String
)

/** Richiesta di registrazione */
data class RegistrationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("profile") val profile: RegistrationProfile
)

/** Dati restituiti dopo la registrazione */
data class RegistrationData(
    @SerializedName("_id") val id: String,
    @SerializedName("profile") val profile: RegistrationProfile
)

/** Risposta alla registrazione */
data class RegistrationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: RegistrationData
)

/** Richiesta di login */
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/** Dati restituiti dopo il login */
data class LoginData(
    @SerializedName("authToken") val authToken: String,
    @SerializedName("userId") val userId: String
)

/** Risposta al login */
data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: LoginData
)

/** Modello per l'IVA associata a un prodotto */
data class NetworkIva(
    @com.google.gson.annotations.SerializedName("_id") val id: String,
    @com.google.gson.annotations.SerializedName("aliquota") val aliquota: String
)
