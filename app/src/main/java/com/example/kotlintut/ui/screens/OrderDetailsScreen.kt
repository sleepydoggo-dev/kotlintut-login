package com.example.kotlintut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kotlintut.data.model.Order
import com.example.kotlintut.ui.components.TotemTopBar
import com.example.kotlintut.ui.theme.Locales
import com.example.kotlintut.viewmodel.CartViewModel
import com.example.kotlintut.viewmodel.AuthViewModel

@Composable
fun OrderDetailsScreen(
    orderId: String,
    viewModel: CartViewModel,
    authViewModel: AuthViewModel,
    language: String,
    onBack: () -> Unit,
    onReorder: (Order) -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }

    // 1. Osserviamo lo stato del ViewModel in modo reattivo
    val cartState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Cerchiamo l'ordine direttamente dalla lista osservata, non tramite chiamata statica
    val order = cartState.orders.find { 
        it.remoteId == orderId || it.orderNumber == orderId || it.id.toString() == orderId 
    }

    // Se l'ordine non c'è e la lista è vuota (es. apertura app da notifica), scarichiamo lo storico
    LaunchedEffect(orderId) {
        if (order == null && cartState.orders.isEmpty()) {
            val user = authViewModel.uiState.value.loggedUser
            if (user != null) {
                viewModel.loadOrders(user)
            }
        }
    }

    Scaffold(
        topBar = {
            TotemTopBar(
                title = translate("details"),
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                showCart = false
            )
        }
    ) { padding ->
        if (order == null) {
            // Se sta caricando o non l'ha trovato, mostra un feedback (puoi anche mettere un CircularProgressIndicator qui)
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(translate("no_orders"), color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                // Header Scontrino
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.date, fontWeight = FontWeight.Bold)
                            Text("€ ${String.format("%.2f", order.total)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Text("${translate("order_id")}${order.orderNumber}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (order.numeroSegnaPosto.isNotBlank()) {
                            Text("📍 Tavolo ${order.numeroSegnaPosto}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        val statusColor = when (order.status.uppercase()) {
                            "PAGATO", "PRONTO", "CONSEGNATO" -> Color(0xFF4CAF50)
                            "DA PAGARE", "IN ATTESA" -> Color(0xFFFF9800)
                            "ANNULLATO", "ERRORE" -> Color.Red
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(order.status, fontWeight = FontWeight.Black, color = statusColor)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(translate("details"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(order.items) { item ->
                        val totalExtrasPrice = item.addedExtras.sumOf { it.price ?: 0.0 } +
                                item.orderAttributes.sumOf { it.price }
                        val basePrice = if (item.price > totalExtrasPrice) item.price - totalExtrasPrice else item.price

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.quantity}x ${item.name}", fontWeight = FontWeight.Bold)
                                Text("€ ${String.format("%.2f", basePrice)}", color = Color.Gray)
                            }

                            item.orderAttributes.forEach { attr ->
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${attr.attributeName}: ${attr.valueName}", fontSize = 13.sp, color = Color.Gray)
                                    if (attr.price > 0) {
                                        Text("€ ${String.format("%.2f", attr.price)}", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }

                            item.removedIngredients.forEach { ing ->
                                Text("  - Senza: ${ing.name}", fontSize = 13.sp, color = Color(0xFFEF5350))
                            }

                            item.addedExtras.forEach { ext ->
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("  + Extra: ${ext.name}", fontSize = 13.sp, color = Color(0xFF4CAF50))
                                    if ((ext.price ?: 0.0) > 0) {
                                        Text("€ ${String.format("%.2f", ext.price)}", fontSize = 13.sp, color = Color(0xFF4CAF50))
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { onReorder(order) },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(translate("reorder"))
                }
            }
        }
    }
} //adsiad