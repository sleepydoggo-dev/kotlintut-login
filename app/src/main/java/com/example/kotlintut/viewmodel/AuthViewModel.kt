package com.example.kotlintut.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintut.data.db.DatabaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Authentication process.
 */
@Immutable
data class AuthUiState(
    val loggedUser: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val prefs = application.getSharedPreferences("TOTEM_PREFS", 0)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val savedUser = prefs.getString("LOGGED_USERNAME", null)
        if (savedUser != null) {
            _uiState.update { it.copy(loggedUser = savedUser) }
        }
    }

    fun login(identifier: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val username = dbHelper.loginAndGetUsername(identifier, pass)
            if (username != null) {
                prefs.edit().putString("LOGGED_USERNAME", username).apply()
                _uiState.update { 
                    it.copy(
                        loggedUser = username, 
                        isLoading = false, 
                        isLoginSuccessful = true 
                    ) 
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Credenziali non valide") }
            }
        }
    }

    fun register(user: String, email: String, pass: String, nome: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            if (dbHelper.userExists(user)) {
                _uiState.update { it.copy(isLoading = false, error = "Username già esistente") }
                return@launch
            }
            
            val id = dbHelper.registerUser(user, email, pass, nome)
            if (id != -1L) {
                prefs.edit().putString("LOGGED_USERNAME", user).apply()
                _uiState.update { 
                    it.copy(
                        loggedUser = user, 
                        isLoading = false, 
                        isLoginSuccessful = true 
                    ) 
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Errore durante la registrazione") }
            }
        }
    }

    fun logout() {
        prefs.edit().remove("LOGGED_USERNAME").apply()
        _uiState.update { it.copy(loggedUser = null, isLoginSuccessful = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
