package com.example.basemvvm.data.response

import com.google.gson.annotations.SerializedName

data class CountryResponse(
    @SerializedName("ip")
    val ip: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("timezone")
    val timezone: String?,
    @SerializedName("country_short")
    val countryShort: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("country_long")
    val countryLong: String?,
)
