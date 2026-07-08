package com.example.kotlintut.data.model

import com.example.kotlintut.data.network.*
import com.google.gson.annotations.SerializedName

/**
 * Rappresenta un elemento nel carrello degli acquisti, includendo il prodotto appiattito e le sue personalizzazioni.
 * L'ordine dei campi riflette l'ordine richiesto dalle API per la serializzazione JSON.
 */
data class CartItem(
    @com.google.gson.annotations.SerializedName("iva") val iva: NetworkIva? = null,
    @SerializedName("quantita") val quantity: Int,
    @SerializedName("nome") val name: String,
    @SerializedName("prezzo") val price: Double,
    @SerializedName("categorie") val categorie: List<String> = emptyList(),
    @SerializedName("categoriaOrigine") val categoriaOrigine: String = "",
    @SerializedName("ingredienti") val ingredients: List<NetworkIngredient> = emptyList(),
    @SerializedName("ingredientiRimossi") val removedIngredients: List<NetworkIngredient> = emptyList(),
    @SerializedName("aggiunte") val addedExtras: List<NetworkExtra> = emptyList(),
    @SerializedName("attributi") val orderAttributes: List<OrderSelectedAttribute> = emptyList(),
    @SerializedName("bevanda") val bevanda: Boolean = false,
    @SerializedName("_id") val elementId: String = "",
    @SerializedName("idProdotto") val id: String,
    @SerializedName("prezzoUnitario") val prezzoUnitario: Double = price,
    @SerializedName("sconto") val sconto: Double = 0.0,
    @SerializedName("daPagare") val daPagare: Double = price,

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
