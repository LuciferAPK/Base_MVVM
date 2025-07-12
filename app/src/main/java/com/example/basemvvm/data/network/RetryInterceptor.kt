package com.example.basemvvm.data.network

import com.example.basemvvm.Logger
import com.example.basemvvm.common.crashlytic.CrashlyticsHelper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class RetryInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Logger.logAction("API request Move in RetryInterceptor")
        val request = chain.request()
        val url = request.url.toUrl().toString()

        return try {
            var response = chain.proceed(request)

            if (!response.isSuccessful && !url.contains("&error=true")) {
                val failedUrl = getFailDomain(url)
                if (failedUrl != url) {
                    response.close()
                    response = chain.proceed(request.newBuilder().url(failedUrl).build())
                }
            }

            response
        } catch (e: SocketTimeoutException) {
            val failedUrl = getFailDomain(url)
            if (failedUrl != url) throw e

            Logger.logAction("Timeout detected! Retrying with fail domain")
            try {
                chain.proceed(request.newBuilder().url(failedUrl).build())
            } catch (e2: Exception) {
                e2.printStackTrace()
                CrashlyticsHelper.recordException(e2)
                throw e2 // Re-throw để OkHttp biết request thất bại
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsHelper.recordException(e)
            throw e
        }
    }

    private fun getFailDomain(url: String): String {
        val failedBaseUrl = "ServerUrlManager.getServerFailedUrl().ifEmpty { return url }"
        val urlPath = when {
            url.contains("/api/") -> {
                url.replaceFirst(
                    "https?://([^/]+)/api/".toRegex(),
                    failedBaseUrl
                ).plus(if (url.contains("?")) "" else "?").plus("&error=true")

            }

            else -> {
                url
            }
        }
        Logger.logAction("getFailDomain: $urlPath")
        return urlPath
    }
}