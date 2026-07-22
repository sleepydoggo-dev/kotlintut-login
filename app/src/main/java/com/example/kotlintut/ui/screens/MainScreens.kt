package com.example.kotlintut.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.data.network.NetworkExtra
import com.example.kotlintut.data.network.NetworkIngredient
import com.example.kotlintut.data.network.NetworkOption
import com.example.kotlintut.ui.components.TotemTopBar

/** Helper per calcolare il numero di colonne della griglia in base alla larghezza dello schermo */
@Composable
fun calculateGridColumns(): Int {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp >= 1200 -> 6 // Monitor grandi / Tablet XL landscape
        configuration.screenWidthDp >= 840 -> 4  // Tablet landscape / Tablet grande portrait
        configuration.screenWidthDp >= 600 -> 3  // Tablet piccolo portrait / Smartphone landscape
        else -> 2 // Smartphone portrait standard
    }
}

/**
 * Categories Screen - Displays the list of available food categories.
 */
/** Schermata che visualizza la griglia delle categorie, gestendo anche la navigazione gerarchica e il tasto indietro del sistema. */
/**
 * Schermata che elenca le categorie principali disponibili per l'ordine.
 */
@Composable
fun CategoriesScreen(
    title: String,
    categories: List<com.example.kotlintut.data.network.NetworkCategory>,
    cartCount: Int,
    showBack: Boolean = false,
    onCategoryClick: (com.example.kotlintut.data.network.NetworkCategory) -> Unit,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    translate: (String) -> String
) {
    // Intercetta il tasto indietro del sistema se non siamo al livello principale
    if (showBack) {
        BackHandler {
            onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            TotemTopBar(
                title = if (showBack) "" else "Ristorante Totem",
                showMenu = !showBack,
                showBack = showBack,
                onBackClick = onBackClick,
                onMenuClick = onMenuClick,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScreenTitle(text = title)

            val columns = calculateGridColumns()
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(categories, key = { index, cat -> "${cat.id ?: ""}_$index" }) { index, category ->
                    val label = remember(category.name, translate) { translate(category.name ?: "") }

                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                scaleIn(initialScale = 0.8f, animationSpec = tween(300, delayMillis = index * 50))
                    ) {
                        CategoryCard(category = category.name ?: "", label = label) { onCategoryClick(category) }
                    }
                }
            }
        }
    }
}

/**
 * Products Screen - Displays products for a specific category.
 */
/** Schermata che elenca i prodotti di una categoria specifica con funzionalità di ricerca e filtraggio. */
@Composable
fun ProductsScreen(
    category: String,
    categoryLabel: String,
    products: List<Product>,
    searchQuery: String,
    cartCount: Int,
    searchLabel: String,
    noProductsLabel: String,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            TotemTopBar(
                title = categoryLabel,
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(searchLabel) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            AnimatedContent(
                targetState = products,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
                },
                label = "ProductsCrossfade"
            ) { targetProducts ->
                if (targetProducts.isEmpty()) {
                    EmptyState(message = noProductsLabel)
                } else {
                    val columns = calculateGridColumns()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(targetProducts, key = { index, prod -> "${prod.id.ifEmpty { prod.name }}_$index" }) { index, product ->
                            var visible by remember(product.id.ifEmpty { product.name }) { mutableStateOf(false) }
                            LaunchedEffect(product.id.ifEmpty { product.name }) { visible = true }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300, delayMillis = index * 30)) +
                                        slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(300, delayMillis = index * 30))
                            ) {
                                ProductCard(product = product) { onProductClick(product) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Product Detail Screen - Allows selection of attributes and quantity.
 */
/** Schermata di dettaglio di un prodotto che permette di personalizzare ingredienti, aggiungere extra e gestire la quantità. */
@Composable
fun ProductDetailScreen(
    product: Product,
    ingredients: List<NetworkIngredient>,
    extras: List<NetworkExtra>,
    isFavorite: Boolean,
    cartCount: Int,
    detailsLabel: String,
    customizeLabel: String,
    addLabel: String,
    onFavoriteToggle: () -> Unit,
    onAddToCart: (Int, List<NetworkIngredient>, List<NetworkExtra>, Map<String, NetworkOption>) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val removedIngredients = remember { mutableStateListOf<NetworkIngredient>() }
    val addedExtras = remember { mutableStateListOf<NetworkExtra>() }
    val selectedAttributes = remember { mutableStateMapOf<String, NetworkOption>() }

    // Inizializza gli attributi con la prima opzione se disponibile
    LaunchedEffect(product) {
        product.attributes.forEach { attr ->
            val values = attr.values
            if (!values.isNullOrEmpty() && !selectedAttributes.containsKey(attr.name)) {
                selectedAttributes[attr.name] = values.first()
            }
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            TotemTopBar(
                title = detailsLabel,
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            )
        },
        bottomBar = {
            ProductPurchaseBar(
                basePrice = product.price,
                quantity = quantity,
                addedExtras = addedExtras,
                selectedAttributes = selectedAttributes.toMap(),
                buttonLabel = addLabel,
                onQuantityIncrease = { quantity++ },
                onQuantityDecrease = { if (quantity > 1) quantity-- },
                onAddToCart = { onAddToCart(quantity, removedIngredients.toList(), addedExtras.toList(), selectedAttributes.toMap()) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ProductHeaderImage(
                product = product,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProductInfo(name = product.name, description = product.description)

            Spacer(modifier = Modifier.height(24.dp))

            product.attributes.forEach { attribute ->
                SingleOptionSelector(
                    title = attribute.name,
                    options = attribute.values ?: emptyList(),
                    selectedOption = selectedAttributes[attribute.name],
                    onOptionSelected = { selectedAttributes[attribute.name] = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (ingredients.isNotEmpty()) {
                IngredientsList(
                    ingredients = ingredients,
                    removedIngredients = removedIngredients,
                    onIngredientToggle = { ing ->
                        if (removedIngredients.contains(ing)) removedIngredients.remove(ing)
                        else removedIngredients.add(ing)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (extras.isNotEmpty()) {
                ExtrasList(
                    extras = extras,
                    addedExtras = addedExtras,
                    onExtraToggle = { ext ->
                        if (addedExtras.contains(ext)) addedExtras.remove(ext)
                        else addedExtras.add(ext)
                    }
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }
}

// --- Sub-Components ---

/** Componente per visualizzare il titolo della schermata corrente. */
@Composable
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

/** Card cliccabile che rappresenta una singola categoria con animazione al tocco. */
@Composable
private fun CategoryCard(category: String, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "PressScale"
    )

    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(category.take(1), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = label,
                modifier = Modifier.padding(12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Card che visualizza l'anteprima di un prodotto, inclusa immagine, nome e prezzo. */
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "PressScale"
    )

    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(0.8f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val context = LocalContext.current
            val imageResId = remember(product.imageKey) {
                context.resources.getIdentifier(product.imageKey, "drawable", context.packageName)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageResId)
                            .crossfade(true)
                            .size(300, 300) // Optimization: Downsample large images
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(product.name.take(1), fontSize = 40.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Text(
                text = product.name,
                modifier = Modifier.padding(8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "€ ${String.format("%.2f", product.price)}",
                modifier = Modifier.padding(bottom = 12.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Barra inferiore per la gestione della quantità e l'aggiunta del prodotto al carrello con calcolo del prezzo totale. */
@Composable
private fun ProductPurchaseBar(
    basePrice: Double,
    quantity: Int,
    addedExtras: List<NetworkExtra>,
    selectedAttributes: Map<String, NetworkOption>,
    buttonLabel: String,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(shadowElevation = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onQuantityDecrease) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(quantity.toString(), fontSize = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = onQuantityIncrease) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onAddToCart,
                modifier = Modifier.height(55.dp)
            ) {
                val extrasTotal = addedExtras.sumOf { it.price ?: 0.0 }
                val attributesExtra = selectedAttributes.values.sumOf { it.price }
                val totalPrice = (basePrice + extrasTotal + attributesExtra) * quantity
                Text("$buttonLabel  € ${String.format("%.2f", totalPrice)}")
            }
        }
    }
}

/** Visualizza l'immagine del prodotto in testata con il pulsante per gestire i preferiti. */
@Composable
private fun ProductHeaderImage(
    product: Product,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(product.imageKey) {
        context.resources.getIdentifier(product.imageKey, "drawable", context.packageName)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != 0) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageResId)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(product.name.take(1), fontSize = 80.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White.copy(alpha = 0.5f), MaterialTheme.shapes.small)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

/** Mostra il nome e la descrizione testuale del prodotto. */
@Composable
private fun ProductInfo(name: String, description: String) {
    Column {
        Text(name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Color.Gray, fontSize = 16.sp)
    }
}

/** Elenca gli ingredienti base del prodotto, permettendo la rimozione di quelli contrassegnati come eliminabili. */
@Composable
private fun IngredientsList(
    ingredients: List<NetworkIngredient>,
    removedIngredients: List<NetworkIngredient>,
    onIngredientToggle: (NetworkIngredient) -> Unit
) {
    Column {
        Text("Ingredienti", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        ingredients.forEach { ing ->
            val isRemovable = ing.isRemovable == "si"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isRemovable) { onIngredientToggle(ing) }
            ) {
                Checkbox(
                    checked = !removedIngredients.contains(ing),
                    onCheckedChange = { if (isRemovable) onIngredientToggle(ing) },
                    enabled = isRemovable
                )
                Text(
                    text = ing.name ?: "",
                    modifier = Modifier.padding(start = 8.dp),
                    color = if (isRemovable) Color.Unspecified else Color.Gray
                )
            }
        }
    }
}

/** Elenca le aggiunte extra opzionali per il prodotto, indicando il relativo sovrapprezzo. */
@Composable
private fun ExtrasList(
    extras: List<NetworkExtra>,
    addedExtras: List<NetworkExtra>,
    onExtraToggle: (NetworkExtra) -> Unit
) {
    Column {
        Text("Aggiunte Extra", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        extras.forEach { ext ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExtraToggle(ext) }
            ) {
                Checkbox(
                    checked = addedExtras.contains(ext),
                    onCheckedChange = { onExtraToggle(ext) }
                )
                Text(
                    text = "${ext.name ?: ""} (+ € ${String.format("%.2f", ext.price ?: 0.0)})",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/** Componente per la selezione di un'opzione singola tramite RadioButton. */
@Composable
fun SingleOptionSelector(
    title: String,
    options: List<NetworkOption>,
    selectedOption: NetworkOption?,
    onOptionSelected: (NetworkOption) -> Unit
) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = { onOptionSelected(option) }
                )
                Text(
                    text = if (option.price > 0) "${option.name} (+ € ${String.format("%.2f", option.price)})" else option.name,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/** Visualizza un messaggio centrato quando una lista di prodotti o categorie risulta vuota. */
/**
 * Visualizza un messaggio quando una lista o una schermata è vuota.
 */
@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center, color = Color.Gray)
    }
}