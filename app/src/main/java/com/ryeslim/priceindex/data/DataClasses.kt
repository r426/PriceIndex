package com.ryeslim.priceindex.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Query(
    @Expose
    @SerializedName("time")
    val time: Time,
    @Expose
    @SerializedName("disclaimer")
    val disclaimer: String,
    @Expose
    @SerializedName("chartName")
    val chartName: String,
    @Expose
    @SerializedName("bpi")
    val bpi: Bpi
)

data class Time(
    @Expose
    @SerializedName("updated")
    val updated: String,
    @Expose
    @SerializedName("updatedISO")
    val updatedIso: String,
    @Expose
    @SerializedName("updateduk")
    val updatedUk: String
)

data class Bpi(
    @Expose
    @SerializedName("USD")
    val usd: Currency,
    @Expose
    @SerializedName("GBP")
    val gbp: Currency,
    @Expose
    @SerializedName("EUR")
    val eur: Currency
)

data class Currency(
    @Expose
    @SerializedName("code")
    val code: String,
    @Expose
    @SerializedName("symbol")
    val symbol: String,
    @Expose
    @SerializedName("rate")
    val rate: String,
    @Expose
    @SerializedName("description")
    val description: String,
    @Expose
    @SerializedName("rate_float")
    val rateFloat: Float
)