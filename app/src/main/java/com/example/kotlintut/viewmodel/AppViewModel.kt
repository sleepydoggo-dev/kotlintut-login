package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kotlintut.ui.theme.Locales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Global application state (Theme, Language, App-wide settings).
 */
data class AppUiState(
    val isDarkMode: Boolean? = null, // null means use system theme
    val language: String = "IT", // IT, EN
    val isReady: Boolean = false,
    val deepLinkOrderId: String? = null
) {
    fun getString(key: String): String {
        return Locales.getString(key, language)
    }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("TOTEM_PREFS", 0)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    /**
     * Inizializza il ViewModel caricando le preferenze salvate di tema e lingua.
     */
    init {
        val darkValue = if (prefs.contains("DARK_MODE")) {
            prefs.getBoolean("DARK_MODE", false)
        } else {
            null
        }
        val lang = prefs.getString("LANGUAGE", "IT") ?: "IT"
        _uiState.update { it.copy(isDarkMode = darkValue, language = lang, isReady = true) }
    }

    /** Alterna tra il tema chiaro e scuro, salvando la preferenza dell'utente nel database locale delle preferenze. */
    fun toggleTheme(currentSystemDark: Boolean) {
        val currentIsDark = uiState.value.isDarkMode ?: currentSystemDark
        val newVal = !currentIsDark
        prefs.edit().putBoolean("DARK_MODE", newVal).apply()
        _uiState.update { it.copy(isDarkMode = newVal) }
    }

    /** Imposta la lingua globale dell'applicazione e ne persiste il valore nelle impostazioni locali. */
    fun setLanguage(lang: String) {
        if (uiState.value.language != lang) {
            prefs.edit().putString("LANGUAGE", lang).apply()
            _uiState.update { it.copy(language = lang) }
        }
    }

    /** Gestione Deep Link da notifiche */
    fun setDeepLinkOrderId(id: String?) {
        _uiState.update { it.copy(deepLinkOrderId = id) }
    }

    fun clearDeepLinkOrderId() {
        _uiState.update { it.copy(deepLinkOrderId = null) }
    }
}
