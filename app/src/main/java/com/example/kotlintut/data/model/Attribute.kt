package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class  Attribute(
    val name: String,
    val extraPrice: Double
) {
    override fun toString(): String {
        return if (extraPrice > 0) "$name (+€ ${String.format("%.2f", extraPrice)})" else name
    }
}
