package com.example.kotlintut.data.model

data class Order(
    val id: Int,
    val date: String,
    val total: Double,
    val items: List<CartItem>
)
