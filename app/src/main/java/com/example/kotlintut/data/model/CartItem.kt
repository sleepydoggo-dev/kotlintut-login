package com.example.kotlintut.data.model

import com.example.kotlintut.data.network.*
import com.google.gson.annotations.SerializedName

/**
 * Rappresenta un elemento nel carrello degli acquisti, includendo il prodotto appiattito e le sue personalizzazioni.
 */
data class CartItem(
    @SerializedName("idProdotto") val id: String,
    @SerializedName("nome") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("quantita") val quantity: Int,
    @SerializedName("attributi") val orderAttributes: List<OrderSelectedAttribute> = emptyList(),
    @SerializedName("aggiunte") val addedExtras: List<NetworkExtra> = emptyList(),
    @SerializedName("ingredientiRimossi") val removedIngredients: List<NetworkIngredient> = emptyList(),
    @SerializedName("ingredienti") val ingredients: List<NetworkIngredient> = emptyList(),
    
    // Campi per uso interno (UI/DB), non inviati al server nel payload ordine
    @Transient val description: String = "",
    @Transient val imageKey: String = "",
    @Transient val category: String = "",
    @Transient val fullAttributesList: List<NetworkAttribute> = emptyList(),
    @Transient val selectedAttributesMap: Map<String, NetworkOption> = emptyMap()
) {
    /**
     * Calcola il prezzo totale dell'elemento considerando quantità, aggiunte extra e attributi selezionati.
     */
    fun getTotalPrice(): Double {
        val extrasTotal = addedExtras.sumOf { it.price }
        val attributesExtra = orderAttributes.sumOf { it.price }
        return (price + extrasTotal + attributesExtra) * quantity
    }
}
