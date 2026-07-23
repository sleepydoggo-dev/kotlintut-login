package com.example.kotlintut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.ui.components.TotemTopBar

/**
 * Schermata per l'inserimento del numero del segnaposto e conferma finale dell'ordine.
 */
@Composable
fun SegnapostoScreen(
    isLoading: Boolean,
    isKioskMode: Boolean = false, // Nuovo parametro per distinguere l'utente
    confirmationNumber: String?,
    error: String?,
    onConfirm: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    var segnaposto by remember { mutableStateOf("") }
    val isConfirmed = confirmationNumber != null

    Scaffold(
        topBar = {
            TotemTopBar(
                title = if (!isConfirmed) "Conferma Ordine" else "Ordine Completato",
                showMenu = false,
                showBack = !isConfirmed && isKioskMode, // Solo superuser può tornare indietro qui
                onBackClick = onBackToHome
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isConfirmed) {
                if (isKioskMode) {
                    // Fase 1: Inserimento Segnaposto (Solo per Superuser)
                    Text(
                        "Inserisci il numero del tuo tavolo o segnaposto",
                        fontSize = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = segnaposto,
                        onValueChange = { if (it.length <= 4) segnaposto = it },
                        label = { Text("Numero Segnaposto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                if (segnaposto.isNotBlank()) {
                                    onConfirm(segnaposto)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            enabled = segnaposto.isNotBlank()
                        ) {
                            Text("CONFERMA ORDINE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Per Utente Standard: Mostra solo caricamento perché l'invio è già partito
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Finalizzazione ordine in corso...")
                    
                    if (error != null) {
                        Text(
                            text = "Errore: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Button(onClick = onBackToHome, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Torna alla Home")
                        }
                    }
                }
            } else {
                // Fase 2: Successo e Numero Ordine
                Text(
                    "Il tuo numero d'ordine è:",
                    fontSize = 24.sp
                )
                
                Text(
                    "#$confirmationNumber",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
                
                Text(
                    "Grazie per il tuo acquisto!",
                    fontSize = 18.sp,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("TORNA ALLA HOME", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
