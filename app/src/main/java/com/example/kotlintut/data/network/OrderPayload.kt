package com.example.kotlintut.data.network

import com.example.kotlintut.data.model.CartItem
import com.google.gson.annotations.SerializedName

/**
 * Rappresenta il payload JSON per l'invio di un ordine in modalità Chiosco.
 */
data class OrderPayload(
    @SerializedName("userId") val userId: String?,
    @SerializedName("prodotti") val prodotti: List<CartItem>,
    @SerializedName("totaleNonScontato") val totaleNonScontato: Double,
    @SerializedName("totale") val totale: Double,
    @SerializedName("pagamento") val pagamento: String = "",
    @SerializedName("origine") val origine: String = "App",
    @SerializedName("numeroSegnaPosto") val numeroSegnaPosto: String,
    @SerializedName("stato") val stato: String = "DA PAGARE",
    @SerializedName("bevande") val bevande: List<CartItem> = emptyList(),
    @SerializedName("food") val food: List<CartItem>
)
