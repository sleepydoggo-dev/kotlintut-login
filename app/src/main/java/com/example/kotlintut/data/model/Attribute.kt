package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

/**
 * Rappresenta un attributo aggiuntivo per un prodotto (es. ingrediente extra).
 */
@Immutable
data class Attribute(
    val name: String,
    val extraPrice: Double
) {
    /**
     * Fornisce una rappresentazione testuale dell'attributo.
     */
    override fun toString(): String {
        return if (extraPrice > 0) "$name (+€ ${String.format("%.2f", extraPrice)})" else name
    }
}
