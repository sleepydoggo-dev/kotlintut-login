package com.example.kotlintut.data.model

data class CartItem(
    val product: Product,
    var quantity: Int,
    val selectedAttributes: List<Attribute> = emptyList()
) {
    fun getTotalPrice(): Double {
        val attributesPrice = selectedAttributes.sumOf { it.extraPrice }
        return (product.price + attributesPrice) * quantity
    }
}
