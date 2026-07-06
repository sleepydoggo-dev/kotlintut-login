package com.example.kotlintut.data.network

import com.google.gson.annotations.SerializedName

/**
 * Wrapper generico per le risposte dell'API
 */
/**
 * Metodo di utilità per creare un oggetto ApiResponse (costruttore implicito gestito da GSON).
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<T>
)

/**
 * Modello categoria ricevuto dal server
 */
/**
 * Modello categoria ricevuto dal server.
 */
data class NetworkCategory(
    @SerializedName("_id") val id: String,
    @SerializedName("categoriaPadre") val parentCategory: com.google.gson.JsonElement?,
    @SerializedName("posizionamento") val position: Int,
    @SerializedName("categoria") val name: String,
    @SerializedName("visibile") val isVisible: Boolean
)

/**
 * Modello ingrediente base
 */
/**
 * Modello ingrediente base.
 */
data class NetworkIngredient(
    @SerializedName("idIngrediente") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("eliminabile") val isRemovable: String // "si" o "no"
)

/**
 * Modello aggiunta extra
 */
/**
 * Modello aggiunta extra.
 */
data class NetworkExtra(
    @SerializedName("idIngrediente") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("prezzo") val price: Double
)

/**
 * Modello opzione singola (formato o dimensione)
 */
/**
 * Modello opzione singola (formato o dimensione).
 */
data class NetworkOption(
    @SerializedName("valore") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("_id") val id: String = ""
)

/**
 * Modello attributo dinamico (es. FORMATO, DIMENSIONE)
 */
/**
 * Modello attributo dinamico (es. FORMATO, DIMENSIONE).
 */
data class NetworkAttribute(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("valori") val values: List<NetworkOption>?
)

/**
 * Modello prodotto ricevuto dal server
 */
/**
 * Modello prodotto ricevuto dal server.
 */
data class NetworkProduct(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("categorie") val categories: List<String>,
    @SerializedName("immagine") val imageUrl: String,
    @SerializedName("disponibile") val isAvailable: Boolean,
    @SerializedName("ingredienti") val ingredients: List<NetworkIngredient>?,
    @SerializedName("aggiunte") val extras: List<NetworkExtra>?,
    @SerializedName("attributi") val attributes: List<NetworkAttribute>?
)
