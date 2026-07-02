package com.example.kotlintut.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotlintut.data.model.Attribute
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.ui.components.TotemTopBar

/**
 * Categories Screen - Displays the list of available food categories.
 */
@Composable
fun CategoriesScreen(
    title: String,
    categories: List<String>,
    cartCount: Int,
    onCategoryClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    translate: (String) -> String
) {
    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = { 
            TotemTopBar(
                onMenuClick = onMenuClick,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScreenTitle(text = title)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(categories, key = { _, cat -> cat }) { index, category ->
                    val label = remember(category, translate) { translate(category) }
                    
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                scaleIn(initialScale = 0.8f, animationSpec = tween(300, delayMillis = index * 50))
                    ) {
                        CategoryCard(category = category, label = label) { onCategoryClick(category) }
                    }
                }
            }
        }
    }
}

/**
 * Products Screen - Displays products for a specific category.
 */
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(targetProducts, key = { _, prod -> prod.name }) { index, product ->
                            var visible by remember(product.name) { mutableStateOf(false) }
                            LaunchedEffect(product.name) { visible = true }

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
@Composable
fun ProductDetailScreen(
    product: Product,
    attributes: List<Attribute>,
    isFavorite: Boolean,
    cartCount: Int,
    detailsLabel: String,
    customizeLabel: String,
    addLabel: String,
    onFavoriteToggle: () -> Unit,
    onAddToCart: (Int, List<Attribute>) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val selectedAttributes = remember { mutableStateListOf<Attribute>() }

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
                selectedAttributes = selectedAttributes,
                buttonLabel = addLabel,
                onQuantityIncrease = { quantity++ },
                onQuantityDecrease = { if (quantity > 1) quantity-- },
                onAddToCart = { onAddToCart(quantity, selectedAttributes.toList()) }
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
            
            if (attributes.isNotEmpty()) {
                AttributeList(
                    attributes = attributes,
                    selectedAttributes = selectedAttributes,
                    customizeLabel = customizeLabel,
                    onAttributeToggle = { attr ->
                        if (selectedAttributes.contains(attr)) selectedAttributes.remove(attr)
                        else selectedAttributes.add(attr)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }
}

// --- Sub-Components ---

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

@Composable
private fun ProductPurchaseBar(
    basePrice: Double,
    quantity: Int,
    selectedAttributes: List<Attribute>,
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
                val totalPrice = (basePrice + selectedAttributes.sumOf { it.extraPrice }) * quantity
                Text("$buttonLabel  € ${String.format("%.2f", totalPrice)}")
            }
        }
    }
}

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

@Composable
private fun ProductInfo(name: String, description: String) {
    Column {
        Text(name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
private fun AttributeList(
    attributes: List<Attribute>,
    selectedAttributes: List<Attribute>,
    customizeLabel: String,
    onAttributeToggle: (Attribute) -> Unit
) {
    Column {
        Text(customizeLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        attributes.forEach { attr ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onAttributeToggle(attr) }
            ) {
                Checkbox(
                    checked = selectedAttributes.contains(attr),
                    onCheckedChange = { onAttributeToggle(attr) }
                )
                Text(attr.toString(), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center, color = Color.Gray)
    }
}
