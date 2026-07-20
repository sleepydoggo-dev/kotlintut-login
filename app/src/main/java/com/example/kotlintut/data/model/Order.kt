package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

/**
 * Modello che rappresenta un ordine completato dall'utente.
 */
@Immutable
data class Order(
    val id: Int,
    val remoteId: String = "",
    val orderNumber: String,
    val date: String,
    val total: Double,
    val status: String,
    val consegna: String = "tavolo",
    val numeroSegnaPosto: String = "",
    val items: List<CartItem>
)
