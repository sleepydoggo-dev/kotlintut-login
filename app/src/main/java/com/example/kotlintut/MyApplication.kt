package com.example.kotlintut

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Abilita i log dettagliati per scovare eventuali errori (rimuovi in produzione)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // Sostituisci la stringa con il tuo App ID di OneSignal
        OneSignal.initWithContext(this, "e246a18d-b4bb-4ac1-b6c2-3f19c623b7ef")

        // Richiede il permesso per le notifiche (obbligatorio su Android 13 e successivi)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }
}