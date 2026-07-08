package com.example.kotlintut.data.network

import com.google.gson.annotations.SerializedName

data class OrderPayload(
    @SerializedName("idUser") val idUser: String?,
    @SerializedName("vouchers") val vouchers: List<String> = emptyList(),
    @SerializedName("prodotti") val prodotti: List<com.example.kotlintut.data.model.CartItem>,
    @SerializedName("scontoCarrelloVoucher") val scontoCarrelloVoucher: Double = 0.0,
    @SerializedName("scontoDaApplicare") val scontoDaApplicare: Double = 0.0,
    @SerializedName("totaleNonScontato") val totaleNonScontato: Double,
    @SerializedName("iva") val ivaTotale: Double,
    @SerializedName("totale") val totale: Double,
    @SerializedName("pagamento") val pagamento: String = " ",
    @SerializedName("origine") val origine: String = "cassa",
    @SerializedName("consegna") val consegna: String = "tavolo",
    @SerializedName("priorita") val priorita: Int = 1,
    @SerializedName("numeroSegnaPosto") val numeroSegnaPosto: String,
    @SerializedName("stato") val stato: String = "DA PAGARE",
    @SerializedName("bevande") val bevande: List<com.example.kotlintut.data.model.CartItem> = emptyList(),
    @SerializedName("food") val food: List<com.example.kotlintut.data.model.CartItem>
)
