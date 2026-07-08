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

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        // Se le preferenze sono state inizializzate, leggiamo i token
        if (::prefs.isInitialized) {
            val token = prefs.getString("AUTH_TOKEN", null)
            val userId = prefs.getString("USER_ID", null)

            if (!token.isNullOrBlank() && !userId.isNullOrBlank()) {
                builder.addHeader("xauthtoken", token)
                builder.addHeader("userid", userId)
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
