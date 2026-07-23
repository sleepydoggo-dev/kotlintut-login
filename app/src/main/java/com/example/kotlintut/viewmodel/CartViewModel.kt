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
import com.example.kotlintut.data.network.RetrofitClient
import com.example.kotlintut.data.network.toDomain
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
    val isOrderConfirmed: Boolean = false,
    val orderConfirmationNumber: String? = null,
    val orderError: String? = null,
    val paymentMethods: List<com.example.kotlintut.data.network.NetworkPaymentMethod> = emptyList()
) {
    val total: Double get() = items.sumOf { it.getTotalPrice() }
    val itemCount: Int get() = items.sumOf { it.quantity }
}

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val prefs = application.getSharedPreferences("TOTEM_PREFS", android.content.Context.MODE_PRIVATE)

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
                iva = product.iva,
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
        
        val dbUser = username ?: "GUEST"
        viewModelScope.launch {
            dbHelper.saveCart(dbUser, currentItems)
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
            val dbUser = username ?: "GUEST"
            viewModelScope.launch {
                dbHelper.saveCart(dbUser, currentItems)
            }
        }
    }

    /** Rimuove definitivamente un elemento dal carrello e sincronizza la modifica sul database locale. */
    fun removeItem(username: String?, item: CartItem) {
        val currentItems = _uiState.value.items.filter { it != item }
        _uiState.update { it.copy(items = currentItems) }
        val dbUser = username ?: "GUEST"
        viewModelScope.launch {
            dbHelper.saveCart(dbUser, currentItems)
        }
    }

    /** Finalizza l'ordine corrente, salvandolo nel database locale e svuotando il carrello dell'utente. */
    fun confirmOrder(username: String) {
        val total = _uiState.value.total
        val items = _uiState.value.items
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            dbHelper.saveOrder(username, total, items, "") // Il numero ordine reale viene gestito in sendOrderToServer
            dbHelper.saveCart(username, emptyList())
            _uiState.update { it.copy(items = emptyList(), isLoading = false, isOrderConfirmed = true) }
        }
    }

    /** Recupera la cronologia di tutti gli ordini effettuati dall'utente loggato chiamando le API del backend. */
    fun loadOrders(username: String) {
        _uiState.update { it.copy(isLoading = true, orderError = null) }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Recuperiamo prima gli ordini locali per avere i numeri d'ordine "random" salvati
                val localOrders = dbHelper.getOrdersByUser(username)
                
                // 2. Chiamata al server
                val response = RetrofitClient.instance.getOrders()
                
                if (response.isSuccessful) {
                    val networkOrders = response.body() ?: emptyList()
                    
                    // Logga il JSON della risposta per debug
                    val gsonPretty = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    val jsonResponse = gsonPretty.toJson(networkOrders)
                    android.util.Log.d("TOTEM_API", "=== STORICO ORDINI DAL SERVER ===\n$jsonResponse")

                    // 3. Mappa gli ordini di rete e tenta di recuperare il numero d'ordine locale se disponibile
                    val domainOrders = networkOrders.map { netOrder ->
                        val domainOrder = netOrder.toDomain()
                        
                        // Cerchiamo se esiste un ordine locale con lo stesso totale e la stessa data (confronto stringa parziale)
                        // Il formato locale è "dd/MM/yyyy HH:mm", quello server ora mappato è lo stesso.
                        val matchingLocal = localOrders.find { 
                            it.total == domainOrder.total && (it.date == domainOrder.date || it.date.contains(domainOrder.date.takeLast(5)))
                        }
                        
                        if (matchingLocal != null && matchingLocal.orderNumber.isNotEmpty()) {
                            android.util.Log.d("TOTEM_API", "Trovato match locale per ordine del ${domainOrder.date}: Uso numero #${matchingLocal.orderNumber}")
                            domainOrder.copy(orderNumber = matchingLocal.orderNumber)
                        } else {
                            domainOrder
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            orders = domainOrders, 
                            isLoading = false 
                        ) 
                    }
                    android.util.Log.d("CartViewModel", "Storico ordini caricato con successo: ${domainOrders.size} ordini")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Errore sconosciuto"
                    android.util.Log.e("CartViewModel", "Errore caricamento ordini (${response.code()}): $errorBody")
                    _uiState.update { it.copy(isLoading = false, orderError = "Errore server (${response.code()})") }
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Errore di rete durante il recupero degli ordini", e)
                _uiState.update { it.copy(isLoading = false, orderError = "Errore di rete: ${e.message}") }
                
                // Fallback locale totale in caso di errore di rete
                val localOrders = dbHelper.getOrdersByUser(username)
                _uiState.update { it.copy(orders = localOrders) }
            }
        }
    }
    
    /** Ottiene i dettagli dei singoli prodotti inclusi in un ordine specifico. */
    fun loadOrderDetails(orderId: Int) {
        viewModelScope.launch {
            val items = dbHelper.getOrderItems(orderId)
            _uiState.update { it.copy(selectedOrderItems = items) }
        }
    }

    /** Cerca un ordine nella lista caricata per ID, Numero Ordine o ID Remoto */
    fun getOrderById(id: String): Order? {
        val order = _uiState.value.orders.find { 
            it.id.toString() == id || it.orderNumber == id
        }
        
        if (order == null) {
            android.util.Log.w("TOTEM_API", "Ordine con ID/Numero $id non trovato nella lista attuale (${_uiState.value.orders.size} ordini)")
        }
        return order
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
        val dbUser = username ?: "GUEST"
        viewModelScope.launch {
            dbHelper.saveCart(dbUser, currentItems)
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
        _uiState.update { it.copy(isOrderConfirmed = false, orderConfirmationNumber = null) }
    }

    /** Carica i metodi di pagamento dal server. */
    fun loadPaymentMethods() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPaymentMethods()
                if (response.isSuccessful) {
                    val methods = response.body() ?: emptyList()
                    _uiState.update { it.copy(paymentMethods = methods.filter { m -> m.attivo }, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /** Svuota completamente il carrello corrente e rimuove i dati salvati nel database per l'utente. */
    fun clearCart(username: String?) {
        _uiState.update { it.copy(items = emptyList()) }
        val dbUser = username ?: "GUEST"
        viewModelScope.launch {
            dbHelper.saveCart(dbUser, emptyList())
        }
    }

    fun sendOrderToServer(
        segnaposto: String = "",
        stato: String = "DA PAGARE",
        pagamento: String = " ",
        datiPagamento: Any? = null
    ) {
        val currentState = _uiState.value
        // Se siamo in modalità Chiosco, inviamo l'ID utente come null per non salvare l'ordine sull'account cliente
        val userId = if (AuthViewModel.IS_KIOSK_MODE) null else prefs.getString("USER_ID", null)
        val authToken = prefs.getString("AUTH_TOKEN", null)
        val loggedUsername = prefs.getString("LOGGED_USERNAME", "GUEST") ?: "GUEST"

        android.util.Log.d("TOTEM_API", "Invio ordine (Kiosk: ${AuthViewModel.IS_KIOSK_MODE}) per userId: $userId, username: $loggedUsername")

        _uiState.update { it.copy(isLoading = true, orderError = null) }

        // Helper per arrotondare a 2 decimali
        fun round(value: Double): Double = String.format(java.util.Locale.US, "%.2f", value).toDouble()

        // Calcolo reale dell'IVA basato sui prodotti nel carrello
        val ivaTotaleRaw = currentState.items.sumOf { item ->
            val aliquota = item.iva?.aliquota?.toDoubleOrNull() ?: 10.0
            val prezzoTotaleItem = item.getTotalPrice()
            prezzoTotaleItem * (aliquota / 100.0)
        }
        val ivaTotale = round(ivaTotaleRaw)

        val (bevande, food) = currentState.items.partition { it.bevanda }

        val payload = com.example.kotlintut.data.network.OrderPayload(
            idUser = userId,
            prodotti = currentState.items,
            totaleNonScontato = round(currentState.total),
            ivaTotale = ivaTotale,
            totale = round(currentState.total),
            numeroSegnaPosto = segnaposto,
            stato = stato,
            pagamento = pagamento,
            datiPagamento = datiPagamento,
            food = food,
            bevande = bevande
        )

        val cartItemSerializer = com.google.gson.JsonSerializer<com.example.kotlintut.data.model.CartItem> { src, _, context ->
            val jsonObject = com.google.gson.JsonObject()
            val extrasPrice = src.addedExtras.sumOf { it.price ?: 0.0 } + src.orderAttributes.sumOf { it.price }
            val priceWithExtras = src.price + extrasPrice
            
            jsonObject.add("iva", context.serialize(src.iva))
            jsonObject.addProperty("quantita", src.quantity)
            jsonObject.addProperty("nome", src.name)
            jsonObject.addProperty("prezzo", priceWithExtras)
            jsonObject.add("categorie", context.serialize(src.categorie))
            jsonObject.addProperty("categoriaOrigine", src.categoriaOrigine)
            jsonObject.add("ingredienti", context.serialize(src.ingredients))
            jsonObject.add("ingredientiRimossi", context.serialize(src.removedIngredients))
            jsonObject.add("aggiunte", context.serialize(src.addedExtras))
            jsonObject.add("attributi", context.serialize(src.orderAttributes))
            jsonObject.addProperty("bevanda", src.bevanda)
            jsonObject.addProperty("_id", src.elementId)
            jsonObject.addProperty("idProdotto", src.id)
            jsonObject.addProperty("prezzoUnitario", priceWithExtras)
            jsonObject.addProperty("sconto", src.sconto)
            jsonObject.addProperty("daPagare", priceWithExtras)
            jsonObject
        }

        val orderSerializer = com.google.gson.JsonSerializer<com.example.kotlintut.data.network.OrderPayload> { src, _, context ->
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("idUser", src.idUser)
            jsonObject.add("vouchers", context.serialize(src.vouchers))
            jsonObject.add("prodotti", context.serialize(src.prodotti))
            jsonObject.addProperty("scontoCarrelloVoucher", src.scontoCarrelloVoucher)
            jsonObject.addProperty("scontoDaApplicare", src.scontoDaApplicare)
            jsonObject.addProperty("totaleNonScontato", src.totaleNonScontato)
            jsonObject.addProperty("iva", src.ivaTotale)
            jsonObject.addProperty("totale", src.totale)
            jsonObject.addProperty("pagamento", src.pagamento)
            jsonObject.addProperty("origine", src.origine)
            jsonObject.addProperty("consegna", src.consegna)
            jsonObject.addProperty("priorita", src.priorita)
            jsonObject.addProperty("numeroSegnaPosto", src.numeroSegnaPosto)
            jsonObject.addProperty("stato", src.stato)
            jsonObject.add("datiPagamento", context.serialize(src.datiPagamento))
            jsonObject.add("bevande", context.serialize(src.bevande))
            jsonObject.add("food", context.serialize(src.food))
            jsonObject
        }

        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(com.example.kotlintut.data.model.CartItem::class.java, cartItemSerializer)
            .registerTypeAdapter(com.example.kotlintut.data.network.OrderPayload::class.java, orderSerializer)
            .create()

        val apiService = com.example.kotlintut.data.network.RetrofitClient.createService(gson)

        viewModelScope.launch {
            try {
                val response = apiService.sendOrder(payload)
                if (response.isSuccessful) {
                    val orderNum = (1..999).random().toString()
                    android.util.Log.d("TOTEM_API", "Ordine inviato con successo! Numero: #$orderNum")
                    
                    // Salviamo l'ordine nel DB locale solo se NON siamo in modalità Chiosco
                    if (!AuthViewModel.IS_KIOSK_MODE) {
                        dbHelper.saveOrder(loggedUsername, currentState.total, currentState.items, orderNum)
                    }

                    dbHelper.saveCart(loggedUsername, emptyList())

                    _uiState.update { 
                        it.copy(
                            items = emptyList(),
                            isLoading = false,
                            isOrderConfirmed = true,
                            orderConfirmationNumber = orderNum
                        ) 
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Errore sconosciuto"
                    android.util.Log.e("TOTEM_API", "Errore invio ordine (${response.code()}): $errorMsg")
                    _uiState.update { it.copy(isLoading = false, orderError = "Errore server (${response.code()})") }
                }
            } catch (e: Exception) {
                android.util.Log.e("TOTEM_API", "Errore di rete durante l'invio dell'ordine", e)
                _uiState.update { it.copy(isLoading = false, orderError = "Errore di rete: ${e.message}") }
            }
        }
    }
}
