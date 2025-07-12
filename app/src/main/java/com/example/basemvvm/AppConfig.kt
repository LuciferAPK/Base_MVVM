package com.example.basemvvm

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.example.basemvvm.common.utils.isAnimationDisabledByUser
import com.example.basemvvm.common.utils.isLowRamDevice
import com.example.basemvvm.config.ConfigManager
import com.example.basemvvm.data.enums.NetworkType
import java.util.Locale

object AppConfig {
    var isAppInForeground = false
    var appLanguage: String = ""

    private fun getDeviceRAM(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    private fun isXhdpiDevice(context: Context) =
        context.resources.displayMetrics.densityDpi == DisplayMetrics.DENSITY_XHIGH

    private fun isCortexA53() = false

    fun isSmallRamDevice(context: Context): Boolean {
        val _4GbRAM = 4 * 1024 * 1024 * 1024L
        return getDeviceRAM(context) <= _4GbRAM || isLowRamDevice(context)
    }

    fun isLowPerformanceDevice(context: Context): Boolean {
        val _4GbRAM = 4 * 1024 * 1024 * 1024L
        val isAndroid11OrLower = Build.VERSION.SDK_INT <= Build.VERSION_CODES.R
        return (isAndroid11OrLower || getDeviceRAM(context) <= _4GbRAM)
                || isLowRamDevice(context) || isCortexA53()
    }

    fun canRunAnim(context: Context): Boolean {
        val isAndroid10OrLower = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
        return !isLowPerformanceDevice(context) && !isAnimationDisabledByUser(context)
                && !isAndroid10OrLower && !isXhdpiDevice(context)
    }

    private fun canRunHeavyAnim(context: Context): Boolean {
        return !isLowPerformanceDevice(context) && !isAnimationDisabledByUser(context)
    }

    fun canRunPreviewAnim(context: Context): Boolean = canRunHeavyAnim(context)

    fun canRunDetailAnim(context: Context): Boolean = false

    fun canRunAnimation(context: Context): Boolean = canRunHeavyAnim(context)

    fun reduceAnimation(context: Context): Boolean {
        val isAndroid10OrLower = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
        return isSmallRamDevice(context) || isAndroid10OrLower
                || isAnimationDisabledByUser(context) || isXhdpiDevice(context) || isCortexA53()
    }

    fun getLocalizedContext(context: Context): Context {
        if (appLanguage.isNotEmpty()) {
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            configuration.locale = Locale(appLanguage)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return ContextWrapper(context)
    }

    fun detectAppLanguage() {
        appLanguage = appRepository().getLanguageSet()
        if (appLanguage.isNotEmpty()) {
            Locale.setDefault(Locale(appLanguage))
            getLocalizedContext(appContext())
        }
    }

    fun isLanguageSetDone() = appLanguage.isNotEmpty()

    fun notLoadNativeList(context: Context): Boolean {
        val _2GbRAM = 2 * 1024 * 1024 * 1024L
        return getDeviceRAM(context) <= _2GbRAM || isLowRamDevice(context)
    }

    fun canShowVideoInListNativeAd(context: Context): Boolean {
        val _3GbRAM = 3 * 1024 * 1024 * 1024L
        return !((getDeviceRAM(context) <= _3GbRAM) || isLowRamDevice(context)) && canShowVideoAd()
    }

    fun canShowVideoAd(): Boolean {
        val context = appContext()
        val _2GbRAM = 2 * 1024 * 1024 * 1024L
        return getDeviceRAM(context) >= _2GbRAM && !isLowRamDevice(context) && !isSlowNetwork()
    }

    fun canPlayVideoInList(): Boolean {
        val context = appContext()
        val isAndroid10OrLower = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
        val _3GbRAM = 3 * 1024 * 1024 * 1024L
        return getDeviceRAM(context) >= _3GbRAM && !isAndroid10OrLower
                && !isLowRamDevice(context)
                && ConfigManager.networkType == NetworkType.FAST
                && !isXhdpiDevice(context)
                && !isCortexA53()

    }

    fun isSlowNetwork(): Boolean {
        return ConfigManager.networkType == NetworkType.SLOW
    }

    fun isDebugMode() = BuildConfig.DEBUG || myApp().activeLog
}