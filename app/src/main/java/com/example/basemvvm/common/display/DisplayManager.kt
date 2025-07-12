package com.example.basemvvm.common.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.media.MediaCodecList
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import com.example.basemvvm.MyApp

object DisplayManager {
    private var isSupportFullHDVideo = false
    private var deviceIsTablet = false
    private var mPhotoRatio: Float = 2.0f

    fun init(context: Context) {
//        isSupportFullHDVideo = isDeviceSupportFullHDVideo()
//        deviceIsTablet = isTabletSize(context)
        mPhotoRatio = getFullScreenRatio()
    }

    fun isGreaterThan1080(context: Context): Boolean {
        val (w, h) = getScreenWidthAndHeight(context)
        return w > 1080 && h > 1080
    }

    fun getImageRatio(): Float {
        return mPhotoRatio
    }

    fun canPlayFullHDVideo() = isSupportFullHDVideo

    fun isTablet() = deviceIsTablet

    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun pxToDp(context: Context, px: Int): Float {
        return px / context.resources.displayMetrics.density
    }

    fun getRatio(width: Int, height: Int): Float = height.toFloat() / width.toFloat()

    fun getFullScreenRatio(): Float {
        val (width, height) = getScreenWidthAndHeight(MyApp.getContext())
        return getRatio(width, height)
    }

    fun getScreenWidthAndHeight(context: Context): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = windowManager.currentWindowMetrics
            displayMetrics.widthPixels = display.bounds.width()
            displayMetrics.heightPixels = display.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)

    }

    fun getScreenDestiny(context: Context): Float {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val display = windowManager.currentWindowMetrics
            displayMetrics.density = display.density
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        return displayMetrics.density

    }

    private fun isDeviceSupportFullHDVideo(): Boolean {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) {
                for (type in info.supportedTypes) {
                    if (type.contains("video/avc", ignoreCase = true)) {
                        val capabilities = info.getCapabilitiesForType(type)
                        val videoCapabilities = capabilities.videoCapabilities
                        if (videoCapabilities != null) {
                            val supportedWidths = videoCapabilities.supportedWidths
                            val supportedHeights = videoCapabilities.supportedHeights
                            return supportedHeights.upper >= 1920 && supportedWidths.upper >= 1080
                        }
                    }
                }
            }
        }
        return false
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        val resourceId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getNavigationBarHeight(context: Context): Int {
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun getActionBarHeight(activity: Activity, resources: Resources): Int {
        val typedValue = TypedValue()
        val actionBarHeight: Int =
            if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
                TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
            } else {
                0
            }
        return actionBarHeight
    }

    private fun isTabletSize(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout
        return (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}