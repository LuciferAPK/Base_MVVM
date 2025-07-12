package com.example.basemvvm.common.utils

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView

fun TextView.setColorGradient(hexColors: List<String>) {
    val width = paint.measureText(text.toString())
    val textShader = LinearGradient(
        0f, 0f, width, height.toFloat(),
        hexColors.map { Color.parseColor(it) }.toIntArray(),
        null, Shader.TileMode.CLAMP
    )
    paint.shader = textShader
    invalidate()
    requestLayout()
}

fun TextView.setColor(hexColor: String) {
    paint.shader = null
    invalidate()
    requestLayout()
    setTextColor(Color.parseColor(hexColor))
}