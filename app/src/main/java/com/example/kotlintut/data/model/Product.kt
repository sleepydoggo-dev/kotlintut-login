package com.example.kotlintut.data.model

data class Product(
    val name: String,
    val price: Double,
    val description: String,
    val imageKey: String,
    val category: String = ""
)
