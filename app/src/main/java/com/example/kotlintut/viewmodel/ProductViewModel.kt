package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintut.data.db.DatabaseHelper
import com.example.kotlintut.data.model.Attribute
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.data.network.NetworkCategory
import com.example.kotlintut.data.network.NetworkExtra
import com.example.kotlintut.data.network.NetworkIngredient
import com.example.kotlintut.data.network.RetrofitClient
import com.example.kotlintut.data.repository.MenuRepository
import com.example.kotlintut.ui.theme.Locales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Product Browsing experience.
 */
@Immutable
data class ProductUiState(
    val categories: List<NetworkCategory> = emptyList(),
    val products: List<Product> = emptyList(),
    val favorites: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: NetworkCategory? = null,
    val selectedProduct: Product? = null,
    val productIngredients: List<NetworkIngredient> = emptyList(),
    val productExtras: List<NetworkExtra> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val language: String = "IT"
) {
    val filteredProducts: List<Product> get() = if (searchQuery.isBlank()) {
        products
    } else {
        products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
}

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val repository = MenuRepository(dbHelper, RetrofitClient.instance)
    private val authPrefs = application.getSharedPreferences("TOTEM_PREFS", 0)
    private val loggedUser: String? get() = authPrefs.getString("LOGGED_USERNAME", null)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<NetworkCategory>>(emptyList())
    val categories: StateFlow<List<NetworkCategory>> = _categories.asStateFlow()

    private val categoryHistory = java.util.Stack<List<NetworkCategory>>()

    init {
        // Carica le categorie all'avvio
        loadCategories()
        loadFavorites()
    }

    /** Carica le categorie principali all'avvio chiamando il repository. */
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { networkCategories ->
                _categories.value = networkCategories
                _uiState.update { it.copy(categories = networkCategories) }
            }
        }
    }

    /** Aggiorna la lingua dell'applicazione e traduce i prodotti e i preferiti attualmente caricati. */
    fun updateLanguage(lang: String) {
        if (_uiState.value.language != lang) {
            _uiState.update { it.copy(language = lang) }
            // Translate current products and favorites
            _uiState.update { state ->
                state.copy(
                    products = state.products.map { translateProduct(it, lang) },
                    favorites = state.favorites.map { translateProduct(it, lang) },
                    selectedProduct = state.selectedProduct?.let { translateProduct(it, lang) }
                )
            }
        }
    }

    /** Recupera i prodotti preferiti dell'utente loggato dal database locale e li traduce. */
    fun loadFavorites() {
        val user = loggedUser
        val lang = _uiState.value.language
        viewModelScope.launch {
            val favs = if (user != null) {
                dbHelper.getFavorites(user).map { translateProduct(it, lang) }
            } else {
                emptyList()
            }
            _uiState.update { it.copy(favorites = favs) }
        }
    }

    /** Gestisce la selezione di una categoria, navigando tra sottocategorie o caricando i prodotti finali. */
    fun selectCategory(category: NetworkCategory, onNavigateToProducts: () -> Unit) {
        val lang = _uiState.value.language
        
        viewModelScope.launch {
            val subCategories = dbHelper.getSubCategoriesLocal(category.id)
            android.util.Log.d("ProductViewModel", "Subcategories for ${category.name} (${category.id}): ${subCategories.size}")
            
            if (subCategories.isNotEmpty()) {
                // Abbiamo sottocategorie: salviamo lo stato attuale e aggiorniamo la lista
                categoryHistory.push(_uiState.value.categories)
                _uiState.update { it.copy(categories = subCategories, selectedCategory = category) }
            } else {
                // Ultimo livello: carichiamo i prodotti
                _uiState.update { it.copy(selectedCategory = category, isLoading = true, products = emptyList()) }
                
                // Usiamo una variabile per navigare una sola volta
                var navigated = false
                repository.getProductsByCategory(category.id).collect { products ->
                    android.util.Log.d("ProductViewModel", "Products collected for ${category.id}: ${products.size}")
                    val translated = products.map { translateProduct(it, lang) }
                    _uiState.update { it.copy(products = translated, isLoading = false) }
                    
                    if (!navigated) {
                        navigated = true
                        onNavigateToProducts()
                    }
                }
            }
        }
    }

    /** Torna al livello precedente nella gerarchia delle categorie, ripristinando la lista precedente dallo stack. */
    fun navigateBackCategory(): Boolean {
        if (!categoryHistory.isEmpty()) {
            val previousCategories = categoryHistory.pop()
            _uiState.update { it.copy(categories = previousCategories, selectedCategory = null) }
            return true
        }
        return false
    }

    /** Verifica se l'utente si trova al livello principale (root) delle categorie. */
    fun isMainLevel(): Boolean {
        return categoryHistory.isEmpty()
    }

    /** Seleziona un prodotto specifico e recupera i suoi ingredienti e le aggiunte extra dal database. */
    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, isLoading = true) }
        viewModelScope.launch {
            try {
                val ingredients = dbHelper.getIngredientsByProduct(product.id)
                val extras = dbHelper.getExtrasByProduct(product.id)
                _uiState.update { 
                    it.copy(
                        productIngredients = ingredients,
                        productExtras = extras,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    /** Aggiunge o rimuove un prodotto dalla lista dei preferiti dell'utente. */
    fun toggleFavorite(product: Product) {
        val user = loggedUser ?: return
        viewModelScope.launch {
            if (dbHelper.isFavorite(user, product.name)) {
                dbHelper.removeFavorite(user, product.name)
            } else {
                dbHelper.addFavorite(user, product)
            }
            loadFavorites()
        }
    }

    /** Controlla se un prodotto con un determinato nome è tra i preferiti dell'utente. */
    fun isFavorite(productName: String): Boolean {
        return uiState.value.favorites.any { it.name == productName }
    }

    /** Aggiorna la stringa di ricerca per filtrare la lista dei prodotti. */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Resetta la selezione del prodotto e svuota le liste di ingredienti e aggiunte. */
    fun clearProductSelection() {
        _uiState.update { it.copy(selectedProduct = null, productIngredients = emptyList(), productExtras = emptyList()) }
    }

    /** Resetta la categoria selezionata e svuota la lista dei prodotti caricati. */
    fun clearCategorySelection() {
        _uiState.update { it.copy(selectedCategory = null, products = emptyList()) }
    }

    /** Traduce il nome e la descrizione di un prodotto nella lingua specificata utilizzando le localizzazioni disponibili. */
    private fun translateProduct(product: Product, lang: String): Product {
        return product.copy(
            name = Locales.getString(product.name, lang),
            description = Locales.getString(product.description, lang),
            attributes = product.attributes.map { attr ->
                attr.copy(
                    name = Locales.getString(attr.name, lang),
                    values = attr.values?.map { opt ->
                        opt.copy(name = Locales.getString(opt.name, lang))
                    } ?: emptyList()
                )
            }
        )
    }
}
