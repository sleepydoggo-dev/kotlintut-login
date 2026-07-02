package com.example.kotlintut.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Product(
    val name: String,
    val price: Double,
    val description: String,
    val imageKey: String,
    val category: String = ""
)
