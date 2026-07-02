package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintut.data.db.DatabaseHelper
import com.example.kotlintut.data.model.Attribute
import com.example.kotlintut.data.model.Product
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
    val categories: List<String> = listOf("Panini", "Primi", "Secondi", "Bevande"),
    val products: List<Product> = emptyList(),
    val favorites: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedProduct: Product? = null,
    val productAttributes: List<Attribute> = emptyList(),
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

    private val _categoryNames = MutableStateFlow<List<String>>(emptyList())
    val categoryNames: StateFlow<List<String>> = _categoryNames.asStateFlow()

    init {
        // Carica le categorie all'avvio
        loadCategories()
        loadFavorites()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { networkCategories ->
                val names = networkCategories.map { it.name }
                _categoryNames.value = names
                _uiState.update { it.copy(categories = names) }
            }
        }
    }

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

    fun selectCategory(category: String) {
        val lang = _uiState.value.language
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
        viewModelScope.launch {
            try {
                // Scarica i prodotti solo quando viene cliccata la categoria
                repository.getProductsByCategory(category).collect { products ->
                    val translated = products.map { translateProduct(it, lang) }
                    _uiState.update { it.copy(products = translated, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, isLoading = true) }
        viewModelScope.launch {
            try {
                val attributes = dbHelper.getAttributesByProduct(product.name)
                _uiState.update { it.copy(productAttributes = attributes, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

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

    fun isFavorite(productName: String): Boolean {
        return uiState.value.favorites.any { it.name == productName }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearProductSelection() {
        _uiState.update { it.copy(selectedProduct = null, productAttributes = emptyList()) }
    }

    fun clearCategorySelection() {
        _uiState.update { it.copy(selectedCategory = null, products = emptyList()) }
    }

    private fun translateProduct(product: Product, lang: String): Product {
        return product.copy(
            name = Locales.getString(product.name, lang),
            description = Locales.getString(product.description, lang)
        )
    }
}
