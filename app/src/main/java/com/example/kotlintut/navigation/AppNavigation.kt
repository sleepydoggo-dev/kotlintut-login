package com.example.kotlintut.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlintut.ui.components.DrawerHeader
import com.example.kotlintut.ui.components.DrawerItem
import com.example.kotlintut.ui.screens.*
import com.example.kotlintut.viewmodel.AppViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object AuthGateway : Screen("auth_gateway")
    object Login : Screen("login")
    object Register : Screen("register")
    object Categories : Screen("categories")
    object Products : Screen("products/{category}") {
        fun createRoute(category: String) = "products/$category"
    }
    object ProductDetail : Screen("product_detail")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object Success : Screen("success")
    object OrderHistory : Screen("order_history")
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var selectedProduct by remember { mutableStateOf<com.example.kotlintut.data.model.Product?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(username = viewModel.loggedUser.value ?: "Ospite")
                Spacer(Modifier.height(12.dp))
                DrawerItem("Home", Icons.Default.Home, true) {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Categories.route)
                }
                if (viewModel.loggedUser.value == null) {
                    DrawerItem("Login", Icons.Default.Login, false) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.AuthGateway.route)
                    }
                } else {
                    DrawerItem("Carrello", Icons.Default.ShoppingCart, false) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Cart.route)
                    }
                    DrawerItem("Storico Ordini", Icons.Default.History, false) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.OrderHistory.route)
                    }
                    Spacer(Modifier.weight(1f))
                    DrawerItem("Logout", Icons.Default.Logout, false) {
                        scope.launch { drawerState.close() }
                        viewModel.logout()
                        navController.navigate(Screen.AuthGateway.route)
                    }
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = if (viewModel.loggedUser.value != null) Screen.Categories.route else Screen.AuthGateway.route
        ) {
            composable(Screen.AuthGateway.route) {
                AuthGatewayScreen(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onRegisterClick = { navController.navigate(Screen.Register.route) },
                    onCancelClick = { navController.navigate(Screen.Categories.route) }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { username ->
                        viewModel.login(username)
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.AuthGateway.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { username ->
                        viewModel.login(username)
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.AuthGateway.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onCategoryClick = { category ->
                        navController.navigate(Screen.Products.createRoute(category))
                    },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable(
                route = Screen.Products.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: "Panini"
                val products = viewModel.getProductsByCategory(category)
                ProductsScreen(
                    category = category,
                    products = products,
                    onProductClick = { product ->
                        selectedProduct = product
                        navController.navigate(Screen.ProductDetail.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ProductDetail.route) {
                selectedProduct?.let { product ->
                    val attributes = viewModel.getAttributesByProduct(product.name)
                    ProductDetailScreen(
                        product = product,
                        attributes = attributes,
                        isFavorite = false,
                        onFavoriteToggle = { /* TODO */ },
                        onAddToCart = { qty, attrs ->
                            viewModel.addToCart(product, qty, attrs)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    items = viewModel.cartItems,
                    total = viewModel.getCartTotal(),
                    onQuantityChange = { item, delta -> viewModel.updateCartItemQuantity(item, delta) },
                    onRemoveItem = { item -> viewModel.removeFromCart(item) },
                    onCheckoutClick = {
                        if (viewModel.loggedUser.value != null) {
                            navController.navigate(Screen.Payment.route)
                        } else {
                            navController.navigate(Screen.AuthGateway.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Payment.route) {
                PaymentScreen(
                    total = viewModel.getCartTotal(),
                    onConfirm = {
                        viewModel.confirmOrder()
                        navController.navigate(Screen.Success.route) {
                            popUpTo(Screen.Categories.route) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Success.route) {
                SuccessScreen(
                    onFinish = {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(Screen.Categories.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OrderHistory.route) {
                OrderHistoryScreen(
                    orders = emptyList(),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
