package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.kotlintut.data.db.DatabaseHelper
import com.example.kotlintut.data.model.CartItem
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.data.model.Attribute

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    
    // User Session
    val loggedUser = mutableStateOf<String?>(null)
    val displayName = mutableStateOf("Ospite")

    // Cart State
    val cartItems = mutableStateListOf<CartItem>()
    
    // UI State
    val currentCategory = mutableStateOf("Panini")

    init {
        val prefs = application.getSharedPreferences("TOTEM_PREFS", 0)
        loggedUser.value = prefs.getString("LOGGED_USERNAME", null)
        if (loggedUser.value != null) {
            loadCart()
        }
    }

    fun login(username: String) {
        loggedUser.value = username
        val prefs = getApplication<Application>().getSharedPreferences("TOTEM_PREFS", 0)
        prefs.edit().putString("LOGGED_USERNAME", username).apply()
        loadCart()
    }

    fun logout() {
        loggedUser.value = null
        displayName.value = "Ospite"
        cartItems.clear()
        val prefs = getApplication<Application>().getSharedPreferences("TOTEM_PREFS", 0)
        prefs.edit().remove("LOGGED_USERNAME").apply()
    }

    fun addToCart(product: Product, quantity: Int, attributes: List<Attribute>) {
        val existingItem = cartItems.find { it.product.name == product.name && it.selectedAttributes == attributes }
        if (existingItem != null) {
            // Re-create the item to trigger State update
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            cartItems.add(CartItem(product, quantity, attributes))
        }
        saveCart()
    }

    fun removeFromCart(item: CartItem) {
        cartItems.remove(item)
        saveCart()
    }

    fun updateCartItemQuantity(item: CartItem, delta: Int) {
        val index = cartItems.indexOf(item)
        if (index != -1) {
            val newQty = item.quantity + delta
            if (newQty > 0) {
                cartItems[index] = item.copy(quantity = newQty)
            } else {
                cartItems.removeAt(index)
            }
            saveCart()
        }
    }

    fun saveCart() {
        loggedUser.value?.let {
            dbHelper.saveCart(it, cartItems)
        }
    }

    private fun loadCart() {
        loggedUser.value?.let {
            cartItems.clear()
            cartItems.addAll(dbHelper.loadCart(it))
        }
    }
    
    fun getCartTotal(): Double {
        return cartItems.sumOf { it.getTotalPrice() }
    }
    
    fun clearCart() {
        cartItems.clear()
        saveCart()
    }
    
    fun confirmOrder() {
        loggedUser.value?.let {
            dbHelper.saveOrder(it, getCartTotal(), cartItems.toList())
            clearCart()
        }
    }

    // Helper for DB queries (categories/products)
    fun getProductsByCategory(category: String): List<Product> {
        return dbHelper.getProductsByCategory(category)
    }

    fun getAttributesByProduct(productName: String): List<Attribute> {
        return dbHelper.getAttributesByProduct(productName)
    }
}
