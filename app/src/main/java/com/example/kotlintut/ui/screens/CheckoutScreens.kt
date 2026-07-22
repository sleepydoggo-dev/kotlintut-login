package com.example.kotlintut.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.data.model.CartItem
import com.example.kotlintut.data.network.NetworkPaymentMethod
import com.example.kotlintut.ui.components.TotemTopBar
import com.example.kotlintut.ui.theme.Locales

/**
 * Schermata del carrello che elenca i prodotti selezionati e il totale.
 */
@Composable
fun CartScreen(
    items: List<CartItem>,
    total: Double,
    language: String,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onCheckoutClick: () -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }

    Scaffold(
        topBar = {
            TotemTopBar(
                title = translate("cart"),
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                showCart = false
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(shadowElevation = 16.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(translate("total"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("€ ${String.format("%.2f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(translate("proceed_to_checkout"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(translate("cart_empty"), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    CartItemRow(item, onQuantityChange, onRemoveItem)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (item.addedExtras.isNotEmpty() || item.orderAttributes.isNotEmpty()) {
                    Text(
                        text = "Personalizzato",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text("€ ${String.format("%.2f", item.getTotalPrice())}", color = Color.Gray, fontSize = 14.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(item, -1) }) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { onQuantityChange(item, 1) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
                IconButton(onClick = { onRemoveItem(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

/**
 * Schermata per la selezione del metodo di pagamento.
 */
@Composable
fun PaymentMethodScreen(
    methods: List<NetworkPaymentMethod>,
    isLoading: Boolean,
    onMethodSelect: (NetworkPaymentMethod) -> Unit,
    onBack: () -> Unit,
    onLoadMethods: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadMethods()
    }

    Scaffold(
        topBar = {
            TotemTopBar(
                title = "Metodo di Pagamento",
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                showCart = false
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Come preferisci pagare?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(methods) { method ->
                        PaymentMethodItem(method = method) {
                            onMethodSelect(method)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(method: NetworkPaymentMethod, onClick: () -> Unit) {
    val icon: ImageVector = when {
        method.nome.contains("Carta", ignoreCase = true) -> Icons.Default.CreditCard
        method.nome.contains("Stripe", ignoreCase = true) -> Icons.Default.CreditCard
        else -> Icons.Default.Payments
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = method.nome,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Schermata per l'inserimento dei dati di contatto per un utente ospite.
 */
@Composable
fun GuestContactScreen(
    name: String,
    email: String,
    language: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }

    Scaffold(
        topBar = {
            TotemTopBar(
                title = translate("contact_info"),
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                showCart = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(translate("guest_info_desc"), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(translate("full_name")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(translate("email")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = name.isNotBlank() && email.contains("@")
            ) {
                Text(translate("continue_to_payment"), fontSize = 18.sp)
            }
        }
    }
}
