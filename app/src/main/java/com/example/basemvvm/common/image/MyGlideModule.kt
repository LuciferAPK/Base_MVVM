package com.example.basemvvm.common.image

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.example.basemvvm.BuildConfig
import com.example.basemvvm.myApp
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule() {

    @SuppressLint("CheckResult")
    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        if (AppConfig.isSmallRamDevice(context)) {
//            val calculator = MemorySizeCalculator.Builder(context)
//                .setBitmapPoolScreens(1f)
//                .build()
//            builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
//            builder.setBitmapPool(LruBitmapPool(calculator.bitmapPoolSize.toLong()))
//        }

        //Compatible with the problem of Register too many Broadcast Receivers on Huawei Tablet 5.1 and 5.0 models
        if (NoConnectivityMonitorFactory.isNeedDisableNetCheck()) {
            builder.setConnectivityMonitorFactory(NoConnectivityMonitorFactory())
        }

        //LOG DEBUG
        if (BuildConfig.DEBUG)
            builder.setLogLevel(Log.DEBUG)
        else builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(
            GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(
                myApp().okHttpClient.get()
            )
        )
    }

    /**
     * @return Set manifest parsing, set to false to avoid adding the same modules twice
     */
    override fun isManifestParsingEnabled() = false
}