package com.example.kotlintut.data.model

data class Attribute(
    val name: String,
    val extraPrice: Double,
    var isSelected: Boolean = false
) {
    override fun toString(): String {
        return if (extraPrice > 0) {
            "$name (+€${String.format("%.2f", extraPrice)})"
        } else name
    }
}
