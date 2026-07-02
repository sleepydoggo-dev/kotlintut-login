package com.example.kotlintut.data.network

import com.google.gson.annotations.SerializedName

/**
 * Wrapper generico per le risposte dell'API
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<T>
)

/**
 * Modello categoria ricevuto dal server
 */
data class NetworkCategory(
    @SerializedName("_id") val id: String,
    @SerializedName("categoriaPadre") val parentCategory: String?,
    @SerializedName("posizionamento") val position: Int,
    @SerializedName("categoria") val name: String,
    @SerializedName("visibile") val isVisible: Boolean
)

/**
 * Modello prodotto ricevuto dal server
 */
data class NetworkProduct(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("categorie") val categories: List<String>,
    @SerializedName("immagine") val imageUrl: String,
    @SerializedName("disponibile") val isAvailable: Boolean
)
