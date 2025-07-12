package com.example.basemvvm.data.network

import com.example.basemvvm.BuildConfig
import com.example.basemvvm.Logger
import com.example.basemvvm.myApp
import okhttp3.Interceptor
import okhttp3.Response

class CustomLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (BuildConfig.DEBUG || myApp().activeLog) {
            val requestLog = StringBuilder()
                .append("➡️ REQUEST: ${request.method} ${request.url}\n")
                .append("Headers:\n")

            request.headers.forEach { header ->
                requestLog.append("  ${header.first}: ${header.second}\n")
            }

            request.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                requestLog.append("Body: ${buffer.readUtf8()}\n")
            }

            Logger.logAction("API_LOG: $requestLog")
        }

        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        val endTime = System.nanoTime()

        if ((BuildConfig.DEBUG || myApp().activeLog) && !(request.url.encodedPath.matches(Regex(".*\\.[a-zA-Z0-9]{2,4}$")))) {
            val responseLog = StringBuilder()
                .append("⬅️ RESPONSE: ${response.code} ${response.message} (${(endTime - startTime) / 1e6} ms)\n ${request.url}\n")
                .append("Headers:\n")

            response.headers.forEach { header ->
                responseLog.append("  ${header.first}: ${header.second}\n")
            }

            kotlin.runCatching {
                val responseBody = response.peekBody(Long.MAX_VALUE)
                responseLog.append("Body:\n${responseBody.string()}")

                Logger.logAction("API_LOG: $responseLog")
            }
        }

        return response
    }
}
