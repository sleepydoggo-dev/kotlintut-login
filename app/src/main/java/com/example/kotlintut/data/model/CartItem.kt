package com.example.kotlintut.data.model

import com.example.kotlintut.data.network.NetworkExtra
import com.example.kotlintut.data.network.NetworkIngredient
import com.example.kotlintut.data.network.NetworkOption

/**
 * Rappresenta un elemento nel carrello degli acquisti, includendo il prodotto e le sue personalizzazioni.
 */
data class CartItem(
    val product: Product,
    val quantity: Int,
    val removedIngredients: List<NetworkIngredient> = emptyList(),
    val addedExtras: List<NetworkExtra> = emptyList(),
    val selectedAttributes: Map<String, NetworkOption> = emptyMap()
) {
    /**
     * Calcola il prezzo totale dell'elemento considerando quantità, aggiunte extra e attributi selezionati.
     */
    fun getTotalPrice(): Double {
        val extrasTotal = addedExtras.sumOf { it.price }
        val attributesExtra = selectedAttributes.values.sumOf { it.price }
        return (product.price + extrasTotal + attributesExtra) * quantity
    }
}
