package com.example.kotlintut.data.network

import com.example.kotlintut.data.model.CartItem
import com.google.gson.annotations.SerializedName

/**
 * Rappresenta il payload JSON per l'invio di un ordine in modalità Chiosco.
 */
data class OrderPayload(
    @SerializedName("origine") val origine: String = "App",
    @SerializedName("stato") val stato: String = "DA PAGARE",
    @SerializedName("prodotti") val prodotti: List<CartItem>,
    @SerializedName("food") val food: List<CartItem>,
    @SerializedName("bevande") val bevande: List<CartItem> = emptyList(),
    @SerializedName("pagamento") val pagamento: String = "",
    @SerializedName("numeroSegnaPosto") val numeroSegnaPosto: String,
    @SerializedName("totale") val totale: Double,
    @SerializedName("totaleNonScontato") val totaleNonScontato: Double
)
