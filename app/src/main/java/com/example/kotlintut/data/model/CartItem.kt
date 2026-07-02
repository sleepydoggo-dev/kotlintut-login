package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedAttributes: List<Attribute> = emptyList()
) {
    fun getTotalPrice(): Double {
        val attrExtra = selectedAttributes.sumOf { it.extraPrice }
        return (product.price + attrExtra) * quantity
    }
}
