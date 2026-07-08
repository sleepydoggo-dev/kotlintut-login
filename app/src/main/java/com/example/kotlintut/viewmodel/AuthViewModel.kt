package com.example.kotlintut.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintut.data.network.LoginRequest
import com.example.kotlintut.data.network.RegistrationProfile
import com.example.kotlintut.data.network.RegistrationRequest
import com.example.kotlintut.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato della UI per il processo di Autenticazione.
 */
@Immutable
data class AuthUiState(
    val loggedUser: String? = null,
    val userId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("TOTEM_PREFS", Context.MODE_PRIVATE)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Inizializza il RetrofitClient con il contesto per l'interceptor
        RetrofitClient.init(application)
        
        // Auto-login rimosso come richiesto. 
        // L'utente deve loggarsi manualmente ogni volta.
    }

    /**
     * Effettua il login simulato loggando il JSON su Logcat.
     */
    fun login(username: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                val request = LoginRequest(username, pass)
                val json = gson.toJson(request)
                
                android.util.Log.d("AUTH_MOCK_LOG", "Login JSON Request:\n$json")
                
                // Simulazione di una risposta positiva per testare la UI
                kotlinx.coroutines.delay(1000)
                
                val mockUserId = "mock_user_id_123"
                val mockToken = "mock_auth_token_456"

                prefs.edit().apply {
                    putString("AUTH_TOKEN", mockToken)
                    putString("USER_ID", mockUserId)
                    putString("LOGGED_USERNAME", username)
                    apply()
                }

                _uiState.update { 
                    it.copy(
                        loggedUser = username,
                        userId = mockUserId,
                        isLoading = false,
                        isLoginSuccessful = true
                    ) 
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error", e)
                _uiState.update { it.copy(isLoading = false, error = "Errore: ${e.message}") }
            }
        }
    }

    /**
     * Registra un nuovo utente simulato loggando il JSON su Logcat.
     * Accetta fullName che viene diviso in firstName e lastName.
     */
    fun register(username: String, email: String, pass: String, fullName: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Divide il fullName in firstName e lastName
                val nameParts = fullName.trim().split(" ")
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                val profile = RegistrationProfile(firstName, lastName)
                val request = RegistrationRequest(email, username, pass, profile)
                
                val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(request)
                
                android.util.Log.d("AUTH_MOCK_LOG", "Registration JSON Request:\n$json")

                // Simulazione di una risposta positiva per testare la UI
                kotlinx.coroutines.delay(1000)
                
                // Dopo la registrazione simulata, eseguiamo il login simulato
                login(username, pass)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Registration error", e)
                _uiState.update { it.copy(isLoading = false, error = "Errore: ${e.message}") }
            }
        }
    }

    /**
     * Disconnette l'utente pulendo i token e lo stato.
     */
    fun logout() {
        prefs.edit().apply {
            remove("AUTH_TOKEN")
            remove("USER_ID")
            remove("LOGGED_USERNAME")
            apply()
        }
        _uiState.update { 
            it.copy(
                loggedUser = null,
                userId = null,
                isLoginSuccessful = false,
                error = null
            ) 
        }
    }

    /**
     * Rimuove eventuali messaggi di errore.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
