package com.example.basemvvm.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.os.postDelayed
import com.example.basemvvm.AppConfig
import com.example.basemvvm.common.crashlytic.CrashlyticsHelper
import com.google.gson.GsonBuilder
import java.time.LocalTime

fun postDelayedSkipException(delay: Long = 0, task: () -> Unit): Runnable {
    return Handler(Looper.getMainLooper()).postDelayed(delay) {
        runCatchException(task) { e -> e.printStackTrace() }
    }
}

fun <T> runCatchException(block: () -> T, catchBlock: (Exception) -> Unit = {}): T? {
    try {
        return block.invoke()
    } catch (e: Exception) {
        catchBlock(e)
    } catch (e: java.lang.IllegalStateException) {
        catchBlock(e)
    } catch (e: java.lang.Exception) {
        catchBlock(e)
    } catch (e: IllegalStateException) {
        catchBlock(e)
    } catch (e: OutOfMemoryError) {
        catchBlock(Exception("Out of memory!"))
    }
    return null
}

@SuppressLint("ClickableViewAccessibility")
fun enableTouch(view: View, isEnable: Boolean, revertAfter: Long = 0L) {
    if (isEnable)
        view.setOnTouchListener { _, _ -> false }
    else
        view.setOnTouchListener { _, _ -> true }
    if (revertAfter > 0) {
        postDelayedSkipException(revertAfter) {
            enableTouch(view, !isEnable)
        }
    }
}

fun safeThreadRunning(block: () -> Unit) {
    fun onException(e: Exception) {
        e.printStackTrace()
        CrashlyticsHelper.recordException(e, "safeThreadRunning-Exception")
    }

    if (Looper.myLooper() == Looper.getMainLooper()) {
        runCatchException(block) { e -> onException(e) }
    } else {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { runCatchException(block) { e -> onException(e) } }
    }
}

fun isMemoryLow(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.lowMemory
}

fun isBatterySaverMode(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isPowerSaveMode
}

fun isBatteryLow(context: Context): Boolean {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return batteryLevel <= 8
}

fun isDeviceIdle(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isDeviceIdleMode
}

fun isLowRamDevice(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.isLowRamDevice
}

fun isAnimationDisabledByUser(context: Context): Boolean {
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        scale == 0.0f
    } catch (e: Settings.SettingNotFoundException) {
        false
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Activity.myRegisterReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
}

fun Context.isDeviceLocked(): Boolean {
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}

fun Any.toPrettyJson(): String {
    try {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(this)
        return json
    } catch (e: Exception) {
        return "Error:${e.message}"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.vibrateOnce(duration: Long = 100) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        val vibrationEffect =
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    } else {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect =
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }
}

fun Context.isOtherAppPlayingMedia(): Boolean {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return audioManager.isMusicActive && !AppConfig.isAppInForeground
}

fun isInQuietHours(): Boolean {
    val now = LocalTime.now()
    val start = LocalTime.of(22, 0) // 10h tối
    val end = LocalTime.of(8, 0) // 8h sáng

    return if (start.isBefore(end)) {
        now.isAfter(start) && now.isBefore(end)
    } else {
        now.isAfter(start) || now.isBefore(end)
    }
}