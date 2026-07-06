package com.example.kotlintut.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.data.model.Product
import com.example.kotlintut.ui.components.TotemTopBar
import com.example.kotlintut.ui.theme.Locales

/**
 * Favorites Screen - Displays products marked as favorites by the user.
 */
/**
 * Schermata che visualizza la lista dei prodotti preferiti dell'utente.
 */
@Composable
fun FavoritesScreen(
    title: String,
    noFavoritesLabel: String,
    favorites: List<Product>,
    cartCount: Int,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            TotemTopBar(
                title = title,
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(noFavoritesLabel, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(favorites, key = { it.name }) { product ->
                    ProductCard(product = product) { onProductClick(product) }
                }
            }
        }
    }
}

/**
 * Options Screen - Manage Theme and Language.
 */
/**
 * Schermata delle opzioni per configurare il tema e la lingua dell'applicazione.
 */
@Composable
fun OptionsScreen(
    title: String,
    aspectLabel: String,
    darkModeLabel: String,
    darkModeDesc: String,
    languageLabel: String,
    appLanguageLabel: String,
    currentLangLabel: String,
    isDarkMode: Boolean,
    language: String,
    cartCount: Int,
    onToggleTheme: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            TotemTopBar(
                title = title,
                showMenu = false,
                showBack = true,
                onBackClick = onBack,
                onCartClick = onCartClick,
                cartItemCount = cartCount
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(aspectLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            ListItem(
                headlineContent = { Text(darkModeLabel) },
                supportingContent = { Text(darkModeDesc) },
                leadingContent = { Icon(Icons.Default.Brightness4, contentDescription = null) },
                trailingContent = {
                    Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() })
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(languageLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            ListItem(
                headlineContent = { Text(appLanguageLabel) },
                supportingContent = { Text("${Locales.getString("current_lang", language)}: $currentLangLabel") },
                leadingContent = { Icon(Icons.Default.Language, contentDescription = null) }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = language == "IT",
                    onClick = { onLanguageChange("IT") },
                    label = { Text("Italiano") }
                )
                FilterChip(
                    selected = language == "EN",
                    onClick = { onLanguageChange("EN") },
                    label = { Text("English") }
                )
            }
        }
    }
}
