package com.example.kotlintut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlintut.navigation.AppNavigation
import com.example.kotlintut.ui.theme.RistoranteTotemTheme
import com.example.kotlintut.viewmodel.AppViewModel
import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationClickEvent

/**
 * Attività principale dell'applicazione che funge da entry point.
 */
class MainActivity : ComponentActivity() {
    /** Punto di ingresso principale dell'attività: configura il tema dell'app e avvia la navigazione Compose. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val appState by appViewModel.uiState.collectAsStateWithLifecycle()

            // OneSignal Click Listener
            val clickListener = remember {
                object : INotificationClickListener {
                    override fun onClick(event: INotificationClickEvent) {
                        val additionalData = event.notification.additionalData
                        if (additionalData != null && additionalData.has("orderId")) {
                            val orderId = additionalData.optString("orderId")
                            if (!orderId.isNullOrBlank()) {
                                appViewModel.setDeepLinkOrderId(orderId)
                            }
                        }
                    }
                }
            }

            DisposableEffect(Unit) {
                OneSignal.Notifications.addClickListener(clickListener)
                onDispose {
                    OneSignal.Notifications.removeClickListener(clickListener)
                }
            }
            
            RistoranteTotemTheme(userPreferenceDark = appState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appViewModel)
                }
            }
        }
    }
}
