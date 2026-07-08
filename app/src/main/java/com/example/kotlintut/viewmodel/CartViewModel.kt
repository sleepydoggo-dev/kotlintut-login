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
import com.example.kotlintut.data.network.OrderPayload
import com.google.gson.Gson
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
        
        // Trasforma gli attributi selezionati nel formato richiesto dal server
        val orderAttributes = selectedAttributes.mapNotNull { (attrName, option) ->
            val attr = product.attributes.find { it.name == attrName }
            if (attr != null) {
                com.example.kotlintut.data.network.OrderSelectedAttribute(
                    attributeId = attr.id,
                    attributeName = attrName,
                    valueId = option.id,
                    valueName = option.name,
                    price = option.price
                )
            } else null
        }

        val existingIndex = currentItems.indexOfFirst { 
            it.name == product.name && 
            it.removedIngredients == removedIngredients && 
            it.addedExtras == addedExtras &&
            it.orderAttributes == orderAttributes
        }

        if (existingIndex != -1) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(CartItem(
                id = product.id,
                name = product.name,
                price = product.price,
                quantity = quantity,
                orderAttributes = orderAttributes,
                addedExtras = addedExtras.map { it.copy(qta = 1) }, // Assicuriamo qta = 1 per le aggiunte
                removedIngredients = removedIngredients.map { it.copy(qta = 1) }, // Assicuriamo qta = 1 per rimossi
                description = product.description,
                imageKey = product.imageKey,
                category = product.category,
                fullAttributesList = product.attributes,
                selectedAttributesMap = selectedAttributes,
                categorie = listOf(product.category),
                categoriaOrigine = product.category
            ))
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
                it.name == newItem.name && 
                it.removedIngredients == newItem.removedIngredients && 
                it.addedExtras == newItem.addedExtras &&
                it.orderAttributes == newItem.orderAttributes
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

    /** Simula l'invio di un ordine in modalità Chiosco, generando un payload JSON e svuotando il carrello. */
    fun inviaOrdineMock(segnaposto: String): String {
        val currentState = _uiState.value
        val payload = OrderPayload(
            prodotti = currentState.items,
            food = currentState.items, // Per ora consideriamo tutto food come nell'esempio
            numeroSegnaPosto = segnaposto,
            totale = currentState.total,
            totaleNonScontato = currentState.total
        )
        
        // Serializzatore personalizzato per garantire l'ordine esatto delle chiavi JSON richiesto dalle API
        val orderSerializer = com.google.gson.JsonSerializer<OrderPayload> { src, _, context ->
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.add("prodotti", context.serialize(src.prodotti))
            jsonObject.addProperty("totaleNonScontato", src.totaleNonScontato)
            jsonObject.addProperty("totale", src.totale)
            jsonObject.addProperty("pagamento", src.pagamento)
            jsonObject.addProperty("origine", src.origine)
            jsonObject.addProperty("numeroSegnaPosto", src.numeroSegnaPosto)
            jsonObject.addProperty("stato", src.stato)
            jsonObject.add("bevande", context.serialize(src.bevande))
            jsonObject.add("food", context.serialize(src.food))
            jsonObject
        }

        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(OrderPayload::class.java, orderSerializer)
            .setPrettyPrinting()
            .create()

        val jsonPayload = gson.toJson(payload)
        
        android.util.Log.d("TOTEM_API_TEST", "POST https://dolcemare.solteconline.it/api/v1/ordine")
        android.util.Log.d("TOTEM_API_TEST", "Payload: $jsonPayload")
        
        // Esegue la chiamata API reale
        val apiService = com.example.kotlintut.data.network.RetrofitClient.createService(gson)
        viewModelScope.launch {
            try {
                val response = apiService.sendOrder(payload)
                if (response.isSuccessful) {
                    android.util.Log.d("TOTEM_API_TEST", "Ordine inviato con successo")
                } else {
                    android.util.Log.e("TOTEM_API_TEST", "Errore invio ordine: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("TOTEM_API_TEST", "Errore di rete invio ordine", e)
            }
        }
        
        // Svuota il carrello
        _uiState.update { it.copy(items = emptyList()) }
        
        // Restituisce un numero d'ordine casuale (mantenuto per compatibilità UI)
        return (1..999).random().toString()
    }
}
