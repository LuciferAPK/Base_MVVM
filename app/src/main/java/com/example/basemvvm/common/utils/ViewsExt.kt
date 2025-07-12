package com.example.basemvvm.common.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import com.example.basemvvm.common.display.DisplayManager

fun View.roundCorner(radiusDp: Float) {
    clipToOutline = radiusDp != 0f
    invalidateOutline()
    this.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0,
                0,
                view.width,
                view.height,
                DisplayManager.dpToPx(view.context, radiusDp).toFloat()
            )
        }
    }
}

fun View.extendClickArea(extraPaddingDp: Float) {
    val extraPadding = DisplayManager.dpToPx(context, extraPaddingDp)
    (parent as? View)?.post {
        val delegateArea = Rect()
        this.getHitRect(delegateArea)
        delegateArea.top -= extraPadding
        delegateArea.left -= extraPadding
        delegateArea.bottom += extraPadding
        delegateArea.right += extraPadding
        (parent as View).touchDelegate = TouchDelegate(delegateArea, this)
    }
}

fun View.extendClickArea(extraPaddingDp: Float, vararg child: View) {
    val extraPadding = DisplayManager.dpToPx(context, extraPaddingDp)
    post {
        val delegates = mutableListOf<TouchDelegate>()
        for (view in child) {
            val delegateArea = Rect()
            view.getHitRect(delegateArea)
            delegateArea.top -= extraPadding
            delegateArea.left -= extraPadding
            delegateArea.bottom += extraPadding
            delegateArea.right += extraPadding
            delegates.add(TouchDelegate(delegateArea, view))
        }
        touchDelegate = MultiTouchDelegate(delegates, this)
    }
}

class MultiTouchDelegate(private val delegates: List<TouchDelegate>, view: View?) :
    TouchDelegate(Rect(), view) {
    override fun onTouchEvent(event: MotionEvent): Boolean {
        for (delegate in delegates) {
            if (delegate.onTouchEvent(event)) {
                return true
            }
        }
        return false
    }
}


var View.isHidden: Boolean
    get() {
        return visibility == View.GONE
    }
    set(value) {
        if (value) {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        } else {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
    }

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.visibleOrGone(visible: Boolean) {
    if (visible) {
        visible()
    } else {
        gone()
    }
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) {
        visible()
    } else {
        invisible()
    }
}

fun Drawable.tint(color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN) {
    mutate().setColorFilter(color, mode)
}

var TextView.drawableStart: Drawable?
    get() = drawables[0]
    set(value) = setDrawables(value, drawableTop, drawableEnd, drawableBottom)

var TextView.drawableTop: Drawable?
    get() = drawables[1]
    set(value) = setDrawables(drawableStart, value, drawableEnd, drawableBottom)

var TextView.drawableEnd: Drawable?
    get() = drawables[2]
    set(value) = setDrawables(drawableStart, drawableTop, value, drawableBottom)

var TextView.drawableBottom: Drawable?
    get() = drawables[3]
    set(value) = setDrawables(drawableStart, drawableTop, drawableEnd, value)

@Deprecated(
    "Consider replace with drawableStart to better support right-to-left Layout",
    ReplaceWith("drawableStart")
)
var TextView.drawableLeft: Drawable?
    get() = compoundDrawables[0]
    set(value) = setCompoundDrawablesWithIntrinsicBounds(
        value,
        drawableTop,
        drawableEnd,
        drawableBottom
    )

@Deprecated(
    "Consider replace with drawableEnd to better support right-to-left Layout",
    ReplaceWith("drawableEnd")
)
var TextView.drawableRight: Drawable?
    get() = compoundDrawables[2]
    set(value) = setCompoundDrawablesWithIntrinsicBounds(
        drawableStart,
        drawableTop,
        value,
        drawableBottom
    )

private val TextView.drawables: Array<Drawable?>
    get() = if (Build.VERSION.SDK_INT >= 17) compoundDrawablesRelative else compoundDrawables

private fun TextView.setDrawables(
    start: Drawable?,
    top: Drawable?,
    end: Drawable?,
    buttom: Drawable?
) {
    if (Build.VERSION.SDK_INT >= 17)
        setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, buttom)
    else
        setCompoundDrawablesWithIntrinsicBounds(start, top, end, buttom)
}

fun View.capture(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}