package com.example.kotlintut.navigation

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlintut.ui.components.DrawerHeader
import com.example.kotlintut.ui.components.DrawerItem
import com.example.kotlintut.ui.screens.*
import com.example.kotlintut.viewmodel.*
import com.example.kotlintut.data.network.NetworkCategory
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object AuthGateway : Screen("auth_gateway")
    object Login : Screen("login")
    object Register : Screen("register")
    object Categories : Screen("categories")
    object Products : Screen("products/{category}") {
        fun createRoute(category: String) = "products/${android.net.Uri.encode(category)}"
    }
    object ProductDetail : Screen("product_detail")
    object Favorites : Screen("favorites")
    object Options : Screen("options")
    object Cart : Screen("cart")
    object GuestContact : Screen("guest_contact")
    object PaymentMethodSelection : Screen("payment_method_selection")
    object StripePayment : Screen("stripe_payment") // Nuova rotta Stripe
    object OrderTracking : Screen("order_tracking")
    object OrderHistory : Screen("order_history")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object Segnaposto : Screen("segnaposto_screen")
}

/** Gestisce la navigazione principale dell'applicazione, configurando il NavHost, le rotte e l'integrazione con i vari ViewModel per sincronizzare lo stato dell'UI. */
@Composable
fun AppNavigation(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val appState by appViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val productState by productViewModel.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()

    // Determiniamo la destinazione iniziale solo una volta al primo avvio
    val startDestination = remember {
        if (authViewModel.uiState.value.loggedUser != null) Screen.Categories.route else Screen.AuthGateway.route
    }

    // Sync state
    LaunchedEffect(appState.language) {
        productViewModel.updateLanguage(appState.language)
    }

    LaunchedEffect(authState.loggedUser) {
        authState.loggedUser?.let { 
            cartViewModel.loadCart(it)
            productViewModel.loadFavorites()
        } ?: run {
            productViewModel.loadFavorites() // Refresh for guest (clear)
        }
    }

    // Handle Deep Linking from Notification
    LaunchedEffect(appState.deepLinkOrderId) {
        appState.deepLinkOrderId?.let { orderId ->
            // Assicuriamoci che gli ordini siano caricati prima di navigare
            if (authState.loggedUser != null && cartState.orders.isEmpty()) {
                cartViewModel.loadOrders(authState.loggedUser!!)
            }
            
            navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                // Puliamo lo stack se necessario
                launchSingleTop = true
            }
            appViewModel.clearDeepLinkOrderId()
        }
    }

    /* Commentato Segnaposto
    LaunchedEffect(cartState.orderConfirmationNumber) {
        if (cartState.orderConfirmationNumber.isNotBlank()) {
            navController.navigate(Screen.OrderTracking.route)
        }
    }
    */

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(
                    username = authState.loggedUser ?: appState.getString("guest"),
                    welcomeText = appState.getString("welcome")
                )
                Spacer(Modifier.height(12.dp))
                
                DrawerItem(appState.getString("home"), Icons.Default.Home, false) {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Categories.route) {
                        popUpTo(Screen.Categories.route) { inclusive = true }
                    }
                }
                
                if (authState.loggedUser != null) {
                    DrawerItem(appState.getString("favorites"), Icons.Default.Favorite, false) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Favorites.route)
                    }
                }

                DrawerItem(appState.getString("options"), Icons.Default.Settings, false) {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Options.route)
                }

                if (authState.loggedUser != null) {
                    DrawerItem(appState.getString("order_history"), Icons.Default.History, false) {
                        scope.launch { drawerState.close() }
                        cartViewModel.loadOrders(authState.loggedUser!!)
                        navController.navigate(Screen.OrderHistory.route)
                    }
                }

                if (authState.loggedUser == null) {
                    DrawerItem(appState.getString("login_register"), Icons.Default.Login, false) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.AuthGateway.route)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                    DrawerItem(appState.getString("logout"), Icons.Default.Logout, false) {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        cartViewModel.clearCart(null)
                        navController.navigate(Screen.AuthGateway.route) {
                            popUpTo(Screen.Categories.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { 
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) 
            },
            exitTransition = { 
                slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) 
            },
            popEnterTransition = { 
                slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) 
            },
            popExitTransition = { 
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) 
            }
        ) {
            composable(Screen.AuthGateway.route) {
                AuthGatewayScreen(
                    language = appState.language,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onRegisterClick = { navController.navigate(Screen.Register.route) },
                    onCancelClick = { 
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.AuthGateway.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    authState = authState,
                    language = appState.language,
                    onLogin = { id, pass -> authViewModel.login(id, pass) },
                    onBack = { 
                        authViewModel.clearError()
                        navController.popBackStack() 
                    }
                )
                if (authState.isLoginSuccessful) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.AuthGateway.route) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authState = authState,
                    language = appState.language,
                    onRegister = { u, e, p, n -> authViewModel.register(u, e, p, n) },
                    onBack = { 
                        authViewModel.clearError()
                        navController.popBackStack() 
                    }
                )
                if (authState.isLoginSuccessful) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.AuthGateway.route) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    title = appState.getString("what_to_order"),
                    categories = productState.categories,
                    cartCount = cartState.itemCount,
                    showBack = !productViewModel.isMainLevel(),
                    onCategoryClick = { category ->
                        productViewModel.selectCategory(category) {
                            navController.navigate(Screen.Products.createRoute(category.id ?: ""))
                        }
                    },
                    onBackClick = { productViewModel.navigateBackCategory() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    translate = { appState.getString(it) }
                )
            }
            composable(
                route = Screen.Products.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("category") ?: ""
                val categoryName = productState.categories.find { it.id == categoryId }?.name ?: categoryId
                
                ProductsScreen(
                    category = categoryId,
                    categoryLabel = appState.getString(categoryName),
                    products = productState.filteredProducts,
                    searchQuery = productState.searchQuery,
                    cartCount = cartState.itemCount,
                    searchLabel = appState.getString("search_products"),
                    noProductsLabel = appState.getString("no_products"),
                    onSearchQueryChange = { productViewModel.onSearchQueryChange(it) },
                    onProductClick = { product ->
                        productViewModel.selectProduct(product)
                        navController.navigate(Screen.ProductDetail.route)
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ProductDetail.route) {
                productState.selectedProduct?.let { product ->
                    ProductDetailScreen(
                        product = product,
                        ingredients = productState.productIngredients,
                        extras = productState.productExtras,
                        isFavorite = productViewModel.isFavorite(product.name),
                        cartCount = cartState.itemCount,
                        detailsLabel = appState.getString("details"),
                        customizeLabel = appState.getString("customize_order"),
                        addLabel = appState.getString("add_to_cart"),
                        onFavoriteToggle = { productViewModel.toggleFavorite(product) },
                        onAddToCart = { qty, removedIngs, addedExts, selectedAttrs ->
                            cartViewModel.addToCart(authState.loggedUser, product, qty, removedIngs, addedExts, selectedAttrs)
                        },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    title = appState.getString("my_favorites"),
                    noFavoritesLabel = appState.getString("no_favorites"),
                    favorites = productState.favorites,
                    cartCount = cartState.itemCount,
                    onProductClick = { product ->
                        productViewModel.selectProduct(product)
                        navController.navigate(Screen.ProductDetail.route)
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Options.route) {
                val isSystemDark = isSystemInDarkTheme()
                OptionsScreen(
                    title = appState.getString("options"),
                    aspectLabel = appState.getString("aspect"),
                    darkModeLabel = appState.getString("dark_mode"),
                    darkModeDesc = appState.getString("toggle_dark_mode"),
                    languageLabel = appState.getString("language"),
                    appLanguageLabel = appState.getString("app_language"),
                    currentLangLabel = appState.getString("current_lang"),
                    isDarkMode = appState.isDarkMode ?: isSystemDark,
                    language = appState.language,
                    cartCount = cartState.itemCount,
                    onToggleTheme = { appViewModel.toggleTheme(isSystemDark) },
                    onLanguageChange = { appViewModel.setLanguage(it) },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    items = cartState.items,
                    total = cartState.total,
                    language = appState.language,
                    onQuantityChange = { item, delta -> cartViewModel.updateQuantity(authState.loggedUser, item, delta) },
                    onRemoveItem = { item -> cartViewModel.removeItem(authState.loggedUser, item) },
                    onCheckoutClick = {
                        // Navighiamo alla selezione del metodo di pagamento
                        navController.navigate(Screen.PaymentMethodSelection.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.GuestContact.route) {
                GuestContactScreen(
                    name = cartState.guestName,
                    email = cartState.guestEmail,
                    language = appState.language,
                    onNameChange = { cartViewModel.updateGuestInfo(it, cartState.guestEmail) },
                    onEmailChange = { cartViewModel.updateGuestInfo(cartState.guestName, it) },
                    onContinue = { navController.navigate(Screen.PaymentMethodSelection.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PaymentMethodSelection.route) {
                PaymentMethodScreen(
                    methods = cartState.paymentMethods,
                    isLoading = cartState.isLoading,
                    onMethodSelect = { method ->
                        if (method.nome.contains("Carta", ignoreCase = true) || method.nome.contains("Stripe", ignoreCase = true)) {
                            navController.navigate(Screen.StripePayment.route)
                        } else {
                            // Contanti o altro: invio ordine diretto con stato DA PAGARE
                            cartViewModel.sendOrderToServer(
                                segnaposto = "",
                                stato = "DA PAGARE",
                                pagamento = method.nome,
                                datiPagamento = null
                            )
                            navController.navigate(Screen.Segnaposto.route)
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onLoadMethods = { cartViewModel.loadPaymentMethods() }
                )
            }
            composable(Screen.StripePayment.route) {
                com.example.kotlintut.ui.screens.PaymentScreen(
                    importo = cartState.total,
                    onPaymentSuccess = { paymentData ->
                        // Invia l'ordine al server come PAGATO con metodo STRIPE e i dati completi del pagamento
                        cartViewModel.sendOrderToServer(
                            segnaposto = "",
                            stato = "PAGATO",
                            pagamento = "stripe",
                            datiPagamento = paymentData
                        ) 
                        navController.navigate(Screen.Segnaposto.route)
                    },
                    onPaymentError = { error ->
                        Log.e("Navigation", "Errore Pagamento Stripe: $error")
                        // In caso di errore NON inviamo l'ordine e non svuotiamo il carrello
                    }
                )
            }
            composable(Screen.OrderTracking.route) {
                OrderTrackingScreen(
                    language = appState.language,
                    onFinish = {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.Categories.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OrderHistory.route) {
                OrderHistoryScreen(
                    orders = cartState.orders,
                    language = appState.language,
                    onNavigateToDetails = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailsScreen(
                    orderId = orderId,
                    viewModel = cartViewModel,
                    authViewModel = authViewModel,
                    language = appState.language,
                    onBack = { navController.popBackStack() },
                    onReorder = { order ->
                        cartViewModel.reorder(authState.loggedUser, order.items)
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }
            /* Commentata rotta Segnaposto
            composable(Screen.Segnaposto.route) {
                SegnapostoScreen(
                    isLoading = cartState.isLoading,
                    confirmationNumber = cartState.orderConfirmationNumber,
                    error = cartState.orderError,
                    onConfirm = { segnaposto ->
                        cartViewModel.sendOrderToServer(segnaposto)
                    },
                    onBackToHome = {
                        cartViewModel.resetOrderConfirmation()
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.Categories.route) { inclusive = true }
                        }
                    }
                )
            }
            */
            
            // Nuova gestione Segnaposto (solo conferma)
            composable(Screen.Segnaposto.route) {
                com.example.kotlintut.ui.screens.SegnapostoScreen(
                    isLoading = cartState.isLoading,
                    confirmationNumber = cartState.orderConfirmationNumber,
                    error = cartState.orderError,
                    onConfirm = { /* Inibito */ },
                    onBackToHome = {
                        cartViewModel.resetOrderConfirmation()
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.Categories.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
