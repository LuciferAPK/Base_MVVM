package com.example.basemvvm.di

import com.example.basemvvm.appContext
import com.example.basemvvm.data.network.Api
import com.example.basemvvm.data.network.CustomLoggingInterceptor
import com.example.basemvvm.data.network.DownloadApi
import com.example.basemvvm.data.network.HeaderInterceptor
import com.example.basemvvm.data.network.NetworkCacheInterceptor
import com.example.basemvvm.data.network.OfflineCacheInterceptor
import com.example.basemvvm.data.network.RetryInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object NetworkModule {

    private var TIME_OUT: Long = 30

    @Provides
    @Reusable
    @JvmStatic
    internal fun provideDownloadApi(): DownloadApi {
        val timeOut = 15L
        val httpClient = OkHttpClient.Builder()
            .callTimeout(timeOut, TimeUnit.MINUTES)
            .readTimeout(timeOut, TimeUnit.MINUTES)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(CustomLoggingInterceptor())
            .retryOnConnectionFailure(true)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://google.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(httpClient.build())
            .build()
        return retrofit.create(DownloadApi::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    internal fun providePostApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {
        val cache = Cache(File(appContext().cacheDir, "http_cache"), 50L * 1024L * 1024L)
        val httpClient = OkHttpClient.Builder()
            .callTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(CustomLoggingInterceptor())
            .addInterceptor(OfflineCacheInterceptor())
            .addInterceptor(RetryInterceptor())
            .addNetworkInterceptor(NetworkCacheInterceptor())
            .cache(cache)
            .retryOnConnectionFailure(true)

        return Retrofit.Builder()
            .baseUrl("https://google.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(httpClient.build())
            .build()
    }

    @Singleton
    @Provides
    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build()
    }
}