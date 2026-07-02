package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Order(
    val id: Int,
    val date: String,
    val total: Double,
    val items: List<CartItem>
)
