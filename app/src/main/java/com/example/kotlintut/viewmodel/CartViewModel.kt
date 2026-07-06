package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintut.data.db.DatabaseHelper
import com.example.kotlintut.data.model.CartItem
import com.example.kotlintut.data.model.Order
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.data.network.NetworkExtra
import com.example.kotlintut.data.network.NetworkIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Shopping Cart and Checkout.
 */
@Immutable
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val selectedOrderItems: List<CartItem> = emptyList(),
    val guestName: String = "",
    val guestEmail: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val isLoading: Boolean = false,
    val isOrderConfirmed: Boolean = false
) {
    val total: Double get() = items.sumOf { it.getTotalPrice() }
    val itemCount: Int get() = items.sumOf { it.quantity }
}

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    /** Carica il carrello salvato dell'utente dal database locale all'avvio o al login. */
    fun loadCart(username: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val items = dbHelper.loadCart(username)
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    /** Aggiunge un prodotto al carrello gestendo il raggruppamento se il prodotto e le personalizzazioni sono identici. */
    fun addToCart(
        username: String?,
        product: Product,
        quantity: Int,
        removedIngredients: List<com.example.kotlintut.data.network.NetworkIngredient>,
        addedExtras: List<com.example.kotlintut.data.network.NetworkExtra>,
        selectedAttributes: Map<String, com.example.kotlintut.data.network.NetworkOption> = emptyMap()
    ) {
        val currentItems = _uiState.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { 
            it.product.name == product.name && 
            it.removedIngredients == removedIngredients && 
            it.addedExtras == addedExtras &&
            it.selectedAttributes == selectedAttributes
        }

        if (existingIndex != -1) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(CartItem(product, quantity, removedIngredients, addedExtras, selectedAttributes))
        }

        _uiState.update { it.copy(items = currentItems) }
        
        username?.let {
            viewModelScope.launch {
                dbHelper.saveCart(it, currentItems)
            }
        }
    }

    /** Modifica la quantità di un elemento già presente nel carrello e aggiorna il database locale. */
    fun updateQuantity(username: String?, item: CartItem, delta: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        val index = currentItems.indexOf(item)
        if (index != -1) {
            val newQty = item.quantity + delta
            if (newQty > 0) {
                currentItems[index] = item.copy(quantity = newQty)
            } else {
                currentItems.removeAt(index)
            }
            _uiState.update { it.copy(items = currentItems) }
            username?.let {
                viewModelScope.launch {
                    dbHelper.saveCart(it, currentItems)
                }
            }
        }
    }

    /** Rimuove definitivamente un elemento dal carrello e sincronizza la modifica sul database locale. */
    fun removeItem(username: String?, item: CartItem) {
        val currentItems = _uiState.value.items.filter { it != item }
        _uiState.update { it.copy(items = currentItems) }
        username?.let {
            viewModelScope.launch {
                dbHelper.saveCart(it, currentItems)
            }
        }
    }

    /** Finalizza l'ordine corrente, salvandolo nel database locale e svuotando il carrello dell'utente. */
    fun confirmOrder(username: String) {
        val total = _uiState.value.total
        val items = _uiState.value.items
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            dbHelper.saveOrder(username, total, items)
            dbHelper.saveCart(username, emptyList())
            _uiState.update { it.copy(items = emptyList(), isLoading = false, isOrderConfirmed = true) }
        }
    }

    /** Recupera la cronologia di tutti gli ordini effettuati dall'utente specificato. */
    fun loadOrders(username: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val orders = dbHelper.getOrdersByUser(username)
            _uiState.update { it.copy(orders = orders, isLoading = false) }
        }
    }
    
    /** Ottiene i dettagli dei singoli prodotti inclusi in un ordine specifico. */
    fun loadOrderDetails(orderId: Int) {
        viewModelScope.launch {
            val items = dbHelper.getOrderItems(orderId)
            _uiState.update { it.copy(selectedOrderItems = items) }
        }
    }

    /** Permette di riaggiungere al carrello tutti i prodotti di un ordine passato, rispettando le personalizzazioni originali. */
    fun reorder(username: String?, orderItems: List<CartItem>) {
        val currentItems = _uiState.value.items.toMutableList()
        orderItems.forEach { newItem ->
            val existingIndex = currentItems.indexOfFirst { 
                it.product.name == newItem.product.name && 
                it.removedIngredients == newItem.removedIngredients && 
                it.addedExtras == newItem.addedExtras &&
                it.selectedAttributes == newItem.selectedAttributes
            }
            if (existingIndex != -1) {
                val existingItem = currentItems[existingIndex]
                currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + newItem.quantity)
            } else {
                currentItems.add(newItem)
            }
        }
        _uiState.update { it.copy(items = currentItems) }
        username?.let {
            viewModelScope.launch {
                dbHelper.saveCart(it, currentItems)
            }
        }
    }

    /** Aggiorna le informazioni di contatto fornite da un utente ospite durante il checkout. */
    fun updateGuestInfo(name: String, email: String) {
        _uiState.update { it.copy(guestName = name, guestEmail = email) }
    }

    /** Aggiorna i dettagli del metodo di pagamento inseriti dall'utente durante la fase finale del checkout. */
    fun updatePaymentInfo(number: String, expiry: String, cvv: String) {
        _uiState.update { it.copy(cardNumber = number, cardExpiry = expiry, cardCvv = cvv) }
    }

    /** Resetta lo stato di conferma dell'ordine per permettere una nuova navigazione pulita. */
    fun resetOrderConfirmation() {
        _uiState.update { it.copy(isOrderConfirmed = false) }
    }
    
    /** Svuota completamente il carrello corrente e rimuove i dati salvati nel database per l'utente. */
    fun clearCart(username: String?) {
        _uiState.update { it.copy(items = emptyList()) }
        username?.let {
            viewModelScope.launch {
                dbHelper.saveCart(it, emptyList())
            }
        }
    }
}
