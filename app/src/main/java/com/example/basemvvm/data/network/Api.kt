package com.example.basemvvm.data.network

import com.example.basemvvm.data.response.CountryResponse
import com.example.basemvvm.data.response.RemoteConfigResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface Api {
    @GET
    suspend fun getCountryCode(
        @Url url: String,
        @Query("lang") lang: String,
    ): CountryResponse

    @GET
    suspend fun getRemoteConfig(
        @Url url: String,
    ): RemoteConfigResponse
}