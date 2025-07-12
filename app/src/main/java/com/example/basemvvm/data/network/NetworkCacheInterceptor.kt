package com.example.basemvvm.data.network

import com.example.basemvvm.Logger
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class NetworkCacheInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Logger.logAction("API request Move in NetworkCacheInterceptor")
        val response = chain.proceed(chain.request())

        if (response.isSuccessful && response.request.method == "GET") {
            val cacheControl = CacheControl.Builder()
                .maxAge(10, TimeUnit.MINUTES)
                .maxStale(1, TimeUnit.HOURS)
                .build()

            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }

        return response.newBuilder()
            .header("Cache-Control", "no-store")
            .build()
    }
}