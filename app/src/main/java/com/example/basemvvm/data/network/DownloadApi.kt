package com.example.basemvvm.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadApi {
    @Streaming
    @GET
    suspend fun downloadFileAsync(@Url fileUrl: String): Response<ResponseBody>
}