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
    val isLoginSuccessful: Boolean = false,
    val isKioskMode: Boolean = AuthViewModel.IS_KIOSK_MODE // Inizializzato dal flag globale
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        /** 
         * FLAG GLOBALE: Imposta a true per abilitare la modalità Chiosco (superuser)
         * Se true, l'account loggato avrà i privilegi di chiosco (salto pagamenti, ecc.)
         */
        const val IS_KIOSK_MODE = false
    }

    private val prefs = application.getSharedPreferences("TOTEM_PREFS", Context.MODE_PRIVATE)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Inizializza il RetrofitClient con il contesto per l'interceptor
        RetrofitClient.init(application)
        
        // Rimuovi dati "mock" residui se presenti
        val savedUserId = prefs.getString("USER_ID", "") ?: ""
        if (savedUserId.startsWith("mock_")) {
            prefs.edit().clear().apply()
            android.util.Log.d("AuthViewModel", "Dati mock rilevati e rimossi")
        }
        
        // Auto-login: se abbiamo uno username e un token salvati, ripristiniamo lo stato
        val savedUsername = prefs.getString("LOGGED_USERNAME", null)
        val savedToken = prefs.getString("AUTH_TOKEN", null)
        
        if (!savedUsername.isNullOrBlank() && !savedToken.isNullOrBlank() && !savedUserId.isNullOrBlank()) {
            android.util.Log.d("AuthViewModel", "Ripristino sessione automatica per: $savedUsername")
            _uiState.update { 
                it.copy(
                    loggedUser = savedUsername,
                    userId = savedUserId,
                    isLoginSuccessful = true
                ) 
            }
        }
    }

    /**
     * Effettua il login reale tramite API Restivus.
     */
    fun login(username: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = api.loginUser(LoginRequest(username, pass))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        val data = body.data
                        
                        android.util.Log.d("TOTEM_API", "=== LOGIN SUCCESS ===")
                        android.util.Log.d("TOTEM_API", "UserId: ${data.userId}")
                        android.util.Log.d("TOTEM_API", "AuthToken: ${data.authToken}")
                        android.util.Log.d("TOTEM_API", "--------------------------------------------------")
                        
                        android.util.Log.d("AUTH_API_LOG", "Login Success!")
                        
                        // Salva i token reali nelle SharedPreferences
                        prefs.edit().apply {
                            putString("AUTH_TOKEN", data.authToken)
                            putString("USER_ID", data.userId)
                            putString("LOGGED_USERNAME", username)
                            apply()
                        }

                        _uiState.update { 
                            it.copy(
                                loggedUser = username,
                                userId = data.userId,
                                isLoading = false,
                                isLoginSuccessful = true
                            ) 
                        }
                    } else {
                        android.util.Log.e("AUTH_API_LOG", "Login Body Error: ${body?.status}")
                        _uiState.update { it.copy(isLoading = false, error = "Login fallito") }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("AUTH_API_LOG", "Login HTTP Error ${response.code()}: $errorBody")
                    _uiState.update { it.copy(isLoading = false, error = "Errore server (${response.code()})") }
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_API_LOG", "Login Network Error", e)
                _uiState.update { it.copy(isLoading = false, error = "Errore di rete: ${e.message}") }
            }
        }
    }

    /**
     * Registra un nuovo utente reale tramite API Restivus.
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
                
                // Log del JSON di richiesta per debug
                val requestJson = com.google.gson.Gson().toJson(request)
                android.util.Log.d("AUTH_API_LOG", "Registration Request JSON:\n$requestJson")

                val response = api.registerUser(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        android.util.Log.d("AUTH_API_LOG", "Registration Success: ${body.data.id}")
                        // Registrazione riuscita, procedi al login automatico reale
                        login(username, pass)
                    } else {
                        val msg = body?.status ?: "Unknown body error"
                        android.util.Log.e("AUTH_API_LOG", "Registration Body Error: $msg")
                        _uiState.update { it.copy(isLoading = false, error = "Registrazione fallita: $msg") }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("AUTH_API_LOG", "Registration HTTP Error ${response.code()}: $errorBody")
                    _uiState.update { it.copy(isLoading = false, error = "Errore registrazione ${response.code()}: $errorBody") }
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_API_LOG", "Registration Network Error", e)
                _uiState.update { it.copy(isLoading = false, error = "Errore di rete: ${e.message}") }
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
