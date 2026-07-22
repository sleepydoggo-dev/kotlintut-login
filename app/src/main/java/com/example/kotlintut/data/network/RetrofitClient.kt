package com.example.kotlintut.data.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://dolcemare.solteconline.it/api/v1/"
    private lateinit var prefs: SharedPreferences

    /** Inizializza il client con il contesto per accedere alle SharedPreferences. */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("TOTEM_PREFS", Context.MODE_PRIVATE)
    }

    private val logging = HttpLoggingInterceptor { message ->
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                val jsonElement = com.google.gson.JsonParser.parseString(message)
                val prettyJson = gson.toJson(jsonElement)
                android.util.Log.d("TOTEM_API", prettyJson)
            } catch (e: Exception) {
                android.util.Log.d("TOTEM_API", message)
            }
        } else if (message.startsWith("-->") || message.startsWith("<--")) {
            // Logga l'inizio e la fine della chiamata (URL e status code)
            android.util.Log.d("TOTEM_API", "--------------------------------------------------")
            android.util.Log.d("TOTEM_API", message)
        } else {
            // Altri metadati (header, ecc.)
            android.util.Log.d("TOTEM_API", message)
        }
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath
        val builder = original.newBuilder()

        // Non aggiungiamo header di auth per login e registrazione
        if (!path.contains("/login") && !path.contains("/users")) {
            if (::prefs.isInitialized) {
                val token = prefs.getString("AUTH_TOKEN", null)
                val userId = prefs.getString("USER_ID", null)

                if (!token.isNullOrBlank() && !userId.isNullOrBlank()) {
                    builder.addHeader("X-Auth-Token", token)
                    builder.addHeader("X-User-Id", userId)
                }
            }
        }

        chain.proceed(builder.build())
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Crea un'istanza di ApiService con un Gson personalizzato. */
    fun createService(gson: com.google.gson.Gson): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
