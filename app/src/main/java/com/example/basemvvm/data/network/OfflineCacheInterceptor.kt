package com.example.basemvvm.data.network

import com.example.basemvvm.Logger
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class OfflineCacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Logger.logAction("API request Move in OfflineCacheInterceptor")
        var request = chain.request()

        val cacheControl = CacheControl.Builder()
            .maxStale(1, TimeUnit.HOURS)
            .build()

        request = request.newBuilder()
            .cacheControl(cacheControl)
            .build()

        return chain.proceed(request)
    }
}