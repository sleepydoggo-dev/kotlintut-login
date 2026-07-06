package com.example.kotlintut.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Barra superiore personalizzata dell'applicazione che gestisce il titolo, il menu laterale, il tasto indietro e l'icona del carrello animata. */
@OptIn(ExperimentalMaterial3Api::class)
/**
 * TopBar personalizzata dell'applicazione con supporto per menu, navigazione indietro e contatore carrello.
 */
@Composable
fun TotemTopBar(
    title: String = "Ristorante Totem",
    showMenu: Boolean = true,
    showBack: Boolean = false,
    cartItemCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    showCart: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Spring Animation for Cart Icon
    var scaleTrigger by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = scaleTrigger,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "CartScale"
    )

    // Trigger animation when count changes
    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0) {
            scaleTrigger = 1.3f
            kotlinx.coroutines.delay(100)
            scaleTrigger = 1f
        }
    }

    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 20.sp) },
        navigationIcon = {
            if (showMenu) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            } else if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            actions()
            if (showCart) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge { Text(cartItemCount.toString()) }
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.scale(scale)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

/** Testata del menu laterale (Drawer) che visualizza il nome dell'utente loggato e un messaggio di benvenuto. */
/**
 * Header per il menu a comparsa (Drawer) che mostra il nome dell'utente loggato.
 */
@Composable
fun DrawerHeader(username: String, welcomeText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = username,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = welcomeText,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

/** Singolo elemento cliccabile del menu laterale con icona e label personalizzata. */
/**
 * Singolo elemento cliccabile all'interno del menu laterale (Drawer).
 */
@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
