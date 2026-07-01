package com.example.kotlintut.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.data.model.CartItem
import com.example.kotlintut.data.model.Order
import com.example.kotlintut.ui.components.TotemTopBar

@Composable
fun CartScreen(
    items: List<CartItem>,
    total: Double,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onCheckoutClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TotemTopBar(title = "Carrello", showMenu = false, showBack = true, onBackClick = onBack) },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(shadowElevation = 16.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Totale", fontSize = 20.sp)
                            Text("€ ${String.format("%.2f", total)}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth().height(55.dp)
                        ) {
                            Text("PROCEDI AL CHECKOUT")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Il carrello è vuoto", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(items) { item ->
                    CartItemRow(item = item, onQuantityChange = onQuantityChange, onRemoveItem = onRemoveItem)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onQuantityChange: (CartItem, Int) -> Unit, onRemoveItem: (CartItem) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (item.selectedAttributes.isNotEmpty()) {
                    Text(item.selectedAttributes.joinToString { it.name }, fontSize = 12.sp, color = Color.Gray)
                }
                Text("€ ${String.format("%.2f", item.getTotalPrice())}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(item, -1) }) {
                    Text("-", fontSize = 20.sp)
                }
                Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { onQuantityChange(item, 1) }) {
                    Text("+", fontSize = 20.sp)
                }
                IconButton(onClick = { onRemoveItem(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(
    total: Double,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Carta") }

    Scaffold(
        topBar = { TotemTopBar(title = "Pagamento", showMenu = false, showBack = true, onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Totale da pagare", fontSize = 18.sp)
            Text("€ ${String.format("%.2f", total)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Scegli metodo di pagamento", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            PaymentMethodRow("Carta di Credito", selectedMethod == "Carta") { selectedMethod = "Carta" }
            PaymentMethodRow("PayPal", selectedMethod == "PayPal") { selectedMethod = "PayPal" }
            PaymentMethodRow("Contanti alla cassa", selectedMethod == "Contanti") { selectedMethod = "Contanti" }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("CONFERMA ORDINE", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PaymentMethodRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 12.dp), fontSize = 16.sp)
    }
}

@Composable
fun SuccessScreen(
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pagamento Effettuato!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Grazie per il tuo acquisto.", fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.width(200.dp).height(55.dp)
        ) {
            Text("FINISCI")
        }
    }
}

@Composable
fun OrderHistoryScreen(
    orders: List<Order>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TotemTopBar(title = "Storico Ordini", showMenu = false, showBack = true, onBackClick = onBack) }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nessun ordine trovato", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(orders) { order ->
                    OrderRow(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderRow(order: Order) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.date, fontWeight = FontWeight.Bold)
                Text("€ ${String.format("%.2f", order.total)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.items.joinToString { "${it.quantity}x ${it.product.name}" },
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
