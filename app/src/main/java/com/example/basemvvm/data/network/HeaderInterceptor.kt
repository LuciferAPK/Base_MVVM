package com.example.basemvvm.data.network

import android.hardware.usb.UsbDevice.getDeviceId
import android.os.Build
import com.example.basemvvm.BuildConfig
import com.example.basemvvm.Logger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.Locale

class HeaderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Logger.logAction("API request Move in HeaderInterceptor")
        val request = chain.request()
        val correctBuilder = request.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("language", Locale.getDefault().language)
//            .addHeader("country", ConfigManager.country)
//            .addHeader("X-NetworkType", ConfigManager.networkType.value)
//            .addHeader("X-DeviceModel", getDeviceModel())
//            .addHeader("X-DeviceName", getDeviceName())
            .addHeader("X-AppId", BuildConfig.APPLICATION_ID)
            .addHeader("deviceId", getDeviceId(Build.MODEL).toString())
//            .addHeader("mobileId", appContext().getMobileId())
//            .addHeader("X-AgeRange", getAgeRangeForBackEnd())
            .addHeader("X-AppType", "android")
//            .addHeader("X-DeviceMemory", memoryByGB.toString())
            .addHeader("X-AppVersion", BuildConfig.VERSION_NAME)
            .addHeader("X-Gender", BuildConfig.VERSION_NAME)
            .addHeader("X-AppVersionCode", BuildConfig.VERSION_CODE.toString())
//                    .addHeader("Accept-Encoding", "zip, deflate, sdch")
            .addHeader("Content-Encoding", "gzip")
            .addHeader("Connection", "close")
        return chain.proceed(correctBuilder.build())
    }
}