package com.example.basemvvm.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.basemvvm.BuildConfig
import com.example.basemvvm.R
import com.example.basemvvm.common.display.DisplayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

fun CardView.setCustomShadow() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
        outlineSpotShadowColor = resources.getColor(R.color.shadowColor, null)
    else cardElevation = DisplayManager.dpToPx(context, 1f).toFloat()
}

fun View.capture(): Bitmap? {
    try {
        return drawToBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun String.padStringToMinLength(minLength: Int = 8): String {
    if (length >= minLength) return this

    val totalPadding = minLength - length
    val leftPadding = totalPadding / 2
    val rightPadding = totalPadding - leftPadding

    return " ".repeat(leftPadding) + this + " ".repeat(rightPadding)
}

fun View.isFullyVisible(percent: Float = 1f): Boolean {
    val rect = Rect()
    val isVisible = getGlobalVisibleRect(rect)
    val viewArea = width * height
    val visibleArea = rect.width() * rect.height()

    return isVisible && (visibleArea >= viewArea * percent)
}

fun View.isNotVisibleOnScreen(): Boolean {
    val rect = Rect()
    val isVisible = getGlobalVisibleRect(rect)
    val viewArea = width * height
    val visibleArea = rect.width() * rect.height()
    return !isVisible || visibleArea <= 0
}

fun shareFile(context: Context, filePath: String) {
    val file = File(filePath)

    if (!file.exists()) {
        return  // Kiểm tra file có tồn tại không
    }

    // Lấy URI từ FileProvider
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    // Tạo Intent để chia sẻ file
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"  // Hoặc "image/*" nếu là ảnh
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền đọc file cho app nhận
    }

    // Mở trình chia sẻ của hệ thống
    context.startActivity(Intent.createChooser(shareIntent, "Share file via"))
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle?.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelable(key, T::class.java)
    } else {
        this?.getParcelable<T>(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle?.getParcelableArrayListCompat(key: String): List<T> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableArrayList(key, T::class.java) ?: emptyList()
    } else {
        this?.getParcelableArrayList<T>(key) ?: emptyList()
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent?.getParcelableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableExtra(key, T::class.java)
    } else {
        this?.getParcelableExtra<T>(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent?.getParcelableArrayListExtraCompat(key: String): List<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableArrayListExtra(key, T::class.java)
    } else {
        this?.getParcelableArrayListExtra<T>(key)
    }
}

fun Fragment.isLiving() = isAdded
        && !isDetached && !isRemoving && lifecycle.currentState.isAtLeast(
    Lifecycle.State.STARTED
)

fun Activity.isLiving() = !this.isFinishing && !this.isDestroyed

fun FragmentManager.isLiving() = !this.isDestroyed && !this.isStateSaved

fun FragmentActivity.isResumed() =
    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && isLiving()

fun Activity.secureScreen() {
    if (!BuildConfig.DEBUG)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
}

fun LifecycleOwner.launchOnceWhenResumed(
    delayed: Long = 0L,
    block: suspend CoroutineScope.(Long) -> Unit
): Job {
    return if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        lifecycleScope.launch {
            if (delayed > 0) delay(delayed)
            block(delayed)
        }
    } else
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (delayed > 0) delay(delayed)
                block(delayed)
                this@launch.cancel()
            }
        }
}

fun LifecycleOwner.launchOnceWhenStarted(block: suspend CoroutineScope.() -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        lifecycleScope.launch { block() }
    } else
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
                this@launch.cancel()
            }
        }
}

fun Fragment.safePopBackStack() {
    if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        if (isAdded) {
            kotlin.runCatching { parentFragmentManager.popBackStack() }
        }
    } else
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (isAdded) {
                    kotlin.runCatching { parentFragmentManager.popBackStack() }
                }
                this@launch.cancel()
            }
        }
}

fun FragmentActivity.safePopBackStack() {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        runCatching { supportFragmentManager.popBackStack() }
    } else {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                runCatching { supportFragmentManager.popBackStack() }
                this@launch.cancel()
            }
        }
    }
}

fun Context.calculateTextWidth(text: String, textSizeDp: Float): Int {
    val textSizePx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        textSizeDp,
        resources.displayMetrics
    )

    val paint = Paint().apply {
        this.textSize = textSizePx
        this.typeface = Typeface.DEFAULT
    }

    return paint.measureText(text).toInt()
}