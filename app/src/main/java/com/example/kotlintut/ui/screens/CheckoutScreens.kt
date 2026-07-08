package com.example.kotlintut.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.data.model.CartItem
import com.example.kotlintut.data.model.Order
import com.example.kotlintut.ui.components.TotemTopBar
import com.example.kotlintut.ui.theme.Locales
import kotlinx.coroutines.delay

/** Schermata del carrello che elenca i prodotti selezionati, permettendo di modificare le quantità, rimuovere elementi e procedere al checkout. */
@OptIn(ExperimentalMaterial3Api::class)
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
        modifier = Modifier.pointerInput(Unit) {},
        topBar = { TotemTopBar(title = translate("cart"), showMenu = false, showBack = true, onBackClick = onBack) },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(shadowElevation = 16.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(translate("total"), fontSize = 20.sp)
                            Text("€ ${String.format("%.2f", total)}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth().height(55.dp)
                        ) {
                            Text(translate("proceed_to_checkout"))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(translate("cart_empty"), fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items, key = { it.name + it.removedIngredients.hashCode() + it.addedExtras.hashCode() + it.orderAttributes.hashCode() }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onRemoveItem(item)
                                true
                            } else false
                        }
                    )
                    
                    val isDismissed = dismissState.currentValue != SwipeToDismissBoxValue.Settled

                    if (!isDismissed) {
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp).background(color, MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 16.dp))
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            content = {
                                CartItemRow(item = item, onQuantityChange = onQuantityChange)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Singola riga del carrello che visualizza i dettagli di un prodotto, incluse le personalizzazioni (rimozioni ed extra) e i controlli di quantità. */
@Composable
fun CartItemRow(item: CartItem, onQuantityChange: (CartItem, Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                // Attributi selezionati (es. Formato, Dimensione)
                item.orderAttributes.forEach { attr ->
                    Text("${attr.attributeName}: ${attr.valueName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }

                // Ingredienti rimossi
                item.removedIngredients.forEach { ing ->
                    Text("Senza: ${ing.name}", fontSize = 12.sp, color = Color.Red)
                }
                
                // Aggiunte extra
                item.addedExtras.forEach { ext ->
                    Text("Extra: ${ext.name} (+ € ${String.format("%.2f", ext.price)})", fontSize = 12.sp, color = Color(0xFF4CAF50))
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
            }
        }
    }
}

/** Schermata per la raccolta delle informazioni di contatto di un utente ospite (nome ed email) prima del pagamento. */
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
        modifier = Modifier.pointerInput(Unit) {},
        topBar = { TotemTopBar(title = translate("contact_info"), showMenu = false, showBack = true, onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(translate("contact_info"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(translate("guest_info_desc"), color = Color.Gray, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(translate("full_name")) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(translate("email")) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = name.isNotBlank() && email.contains("@")
            ) {
                Text(translate("continue_to_payment"))
            }
        }
    }
}

/** Schermata di pagamento che permette di scegliere il metodo di pagamento e inserire i dettagli della carta di credito. */
@Composable
fun PaymentScreen(
    total: Double,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String,
    language: String,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var selectedMethod by remember { mutableStateOf("Carta") }

    val isExpiryValid = cardExpiry.length == 5 && cardExpiry.contains("/")
    val isCvvValid = cardCvv.length == 3
    val isCardValid = cardNumber.length >= 16 && isExpiryValid && isCvvValid

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = { TotemTopBar(title = translate("payment"), showMenu = false, showBack = true, onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            Text(translate("total_to_pay"), fontSize = 18.sp)
            Text("€ ${String.format("%.2f", total)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PaymentMethodRow(translate("credit_card"), selectedMethod == "Carta") { selectedMethod = "Carta" }
            
            AnimatedVisibility(
                visible = selectedMethod == "Carta",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) onCardNumberChange(it) },
                        label = { Text(translate("card_number")) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { input ->
                                val clean = input.replace("/", "")
                                if (clean.length <= 4 && clean.all { it.isDigit() }) {
                                    val formatted = if (clean.length >= 2) {
                                        clean.substring(0, 2) + "/" + clean.substring(2)
                                    } else {
                                        clean
                                    }
                                    onCardExpiryChange(formatted)
                                }
                            },
                            label = { Text(translate("expiry")) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("MM/YY") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) onCardCvvChange(it) },
                            label = { Text(translate("cvv")) },
                            modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
            
            PaymentMethodRow("PayPal", selectedMethod == "PayPal") { selectedMethod = "PayPal" }
            PaymentMethodRow("Contanti alla cassa", selectedMethod == "Contanti") { selectedMethod = "Contanti" }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = if (selectedMethod == "Carta") isCardValid else true
            ) {
                Text(translate("confirm_order"), fontSize = 18.sp)
            }
        }
    }
}

/** Riga selezionabile per la scelta del metodo di pagamento con un pulsante radio. */
/**
 * Visualizza un singolo metodo di pagamento selezionabile.
 */
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

/** Schermata di monitoraggio dello stato dell'ordine con un indicatore di progresso circolare animato. */
@Composable
fun OrderTrackingScreen(
    language: String,
    onFinish: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var statusKey by remember { mutableStateOf("order_received") }
    var progress by remember { mutableFloatStateOf(0.3f) }

    LaunchedEffect(Unit) {
        delay(3000)
        statusKey = "order_preparing"
        progress = 0.6f
        delay(4000)
        statusKey = "order_ready"
        progress = 1.0f
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(150.dp),
            strokeWidth = 12.dp,
            color = if (progress >= 1.0f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(translate(statusKey), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(translate("order_status"), color = Color.Gray)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        if (progress >= 1.0f) {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text(translate("back_to_home"))
            }
        }
    }
}

/** Schermata che visualizza la cronologia degli ordini passati dell'utente con possibilità di rieseguire un ordine. */
@Composable
fun OrderHistoryScreen(
    orders: List<Order>,
    language: String,
    onReorder: (List<CartItem>) -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(translate("details")) },
            text = { Text(translate("order_history_info")) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(translate("ok"))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = { 
            TotemTopBar(
                title = translate("order_history"), 
                showMenu = false, 
                showBack = true, 
                onBackClick = onBack,
                showCart = false,
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            ) 
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(translate("no_orders"), fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderRowWithDetails(order = order, language = language, onReorder = { onReorder(order.items) })
                }
            }
        }
    }
}

/** Riga della cronologia ordini che può essere espansa per visualizzare i dettagli dei prodotti inclusi e le loro personalizzazioni. */
@Composable
fun OrderRowWithDetails(order: Order, language: String, onReorder: () -> Unit) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(order.date, fontWeight = FontWeight.Bold)
                    Text("${translate("order_id")}${order.id}", fontSize = 12.sp, color = Color.Gray)
                }
                Text("€ ${String.format("%.2f", order.total)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    order.items.forEach { item ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.quantity}x ${item.name}", fontSize = 14.sp)
                                Text("€ ${String.format("%.2f", item.getTotalPrice())}", fontSize = 14.sp)
                            }
                            
                            // Attributi selezionati
                            if (item.orderAttributes.isNotEmpty()) {
                                val options = item.orderAttributes.joinToString(" - ") { "${it.attributeName}: ${it.valueName}" }
                                Text("  $options", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            item.removedIngredients.forEach { ing ->
                                Text("  - Senza: ${ing.name}", fontSize = 12.sp, color = Color.Red)
                            }
                            item.addedExtras.forEach { ext ->
                                Text("  + Extra: ${ext.name}", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onReorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(translate("reorder"))
                    }
                }
            }
        }
    }
}
