package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable
import com.example.kotlintut.data.network.NetworkAttribute
import com.example.kotlintut.data.network.NetworkIva

/**
 * Modello di dominio per un prodotto del menu.
 */
@Immutable
data class Product(
    val id: String = "",
    val name: String,
    val price: Double,
    val description: String,
    val imageKey: String,
    val category: String = "",
    val attributes: List<NetworkAttribute> = emptyList(),
    val iva: NetworkIva? = null
)
