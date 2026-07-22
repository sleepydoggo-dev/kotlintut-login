package com.example.kotlintut.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kotlintut.data.network.RetrofitClient
import com.google.gson.JsonObject
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * PaymentScreen gestisce il flusso reale di pagamento Stripe.
 * Converte l'importo in centesimi, richiede il PaymentIntent al server
 * e presenta il PaymentSheet di Stripe.
 *
 * @param importo Il totale in Euro (es. 8.90)
 * @param onPaymentSuccess Callback per pagamento completato, restituisce il JSON dei dati di pagamento
 * @param onPaymentError Callback per errore o annullamento
 */
@Composable
fun PaymentScreen(
    importo: Double,
    onPaymentSuccess: (JsonObject) -> Unit,
    onPaymentError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 1. Inizializzazione Stripe
    LaunchedEffect(Unit) {
        PaymentConfiguration.init(context, "pk_test_aimL58BU3S9Hi5A2eHQQFdNr00CoFfh9xw")
    }

    var isLoading by remember { mutableStateOf(false) }
    var currentPaymentIntentId by remember { mutableStateOf<String?>(null) }
    var fullPaymentData by remember { mutableStateOf<JsonObject?>(null) }
    
    // 2. Registrazione del PaymentSheet per gestire i risultati
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                Log.d("Stripe", "Pagamento completato con successo!")
                fullPaymentData?.let { onPaymentSuccess(it) } ?: onPaymentError("Dati pagamento mancanti")
            }
            is PaymentSheetResult.Canceled -> {
                Log.d("Stripe", "Pagamento annullato dall'utente.")
                // ANNULLAMENTO SUL BACKEND
                currentPaymentIntentId?.let { id ->
                    scope.launch {
                        try {
                            RetrofitClient.instance.cancelStripePayment(mapOf("idPagamento" to id))
                            Log.d("Stripe", "Backend notificato dell'annullamento ID: $id")
                        } catch (e: Exception) {
                            Log.e("Stripe", "Errore annullamento backend: ${e.message}")
                        }
                    }
                }
                onPaymentError("Pagamento annullato.")
            }
            is PaymentSheetResult.Failed -> {
                val msg = result.error.message ?: "Errore Stripe"
                Log.e("Stripe", "Pagamento fallito: $msg")
                onPaymentError(msg)
            }
        }
        isLoading = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Riepilogo Checkout", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "€ ${String.format(Locale.getDefault(), "%.2f", importo)}",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            // TRADUZIONE IN CENTESIMI: 8.90 -> "890"
                            val centesimi = (importo * 100).toLong().toString()
                            Log.d("Stripe", "Invio richiesta POST con importo: $centesimi centesimi")

                            // 3. Chiamata POST al tuo backend
                            val response = RetrofitClient.instance.createStripePayment(
                                mapOf("importo" to centesimi)
                            )
                            
                            if (response.isSuccessful && response.body() != null) {
                                val stripeResponse = response.body()!!
                                val stripeData = stripeResponse.data
                                val clientSecret = stripeData.get("client_secret").asString
                                
                                // Salviamo i dati per il successo e l'ID per l'annullamento
                                fullPaymentData = stripeData
                                currentPaymentIntentId = stripeData.get("id").asString

                                Log.d("Stripe", "Ricevuto ID: $currentPaymentIntentId. Apro PaymentSheet...")
                                
                                // 4. PRESENTAZIONE DELLA UI DI STRIPE
                                paymentSheet.presentWithPaymentIntent(
                                    paymentIntentClientSecret = clientSecret,
                                    configuration = PaymentSheet.Configuration(
                                        merchantDisplayName = "Dolce Mare Ristorante",
                                        allowsDelayedPaymentMethods = false
                                    )
                                )
                            } else {
                                isLoading = false
                                onPaymentError("Errore dal server durante la creazione del pagamento.")
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            Log.e("Stripe", "Errore di rete: ${e.message}")
                            onPaymentError("Controlla la tua connessione internet.")
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("PROCEDI AL PAGAMENTO")
                }
            }
        }
    }
}
