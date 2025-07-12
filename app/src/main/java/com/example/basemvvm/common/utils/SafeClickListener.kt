package com.example.basemvvm.common.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.SystemClock
import android.view.View

class SafeClickListener(
    private var defaultInterval: Int = 700,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

class SafeClickInTroNextListener(
    private var defaultInterval: Int = 500,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun View.setSafeOnClickNextIntroListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickInTroNextListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun View.setSafeOnClickScaleEffect(scale: Float = 0.95f, onClick: () -> Unit) {
    ClickScaleEffect(onClick).applyTo(this, scale)
}

class ClickScaleEffect(private val onClick: () -> Unit) {

    fun applyTo(view: View, scale: Float) {
        view.setSafeOnClickListener { v ->
            val scaleXDown = ObjectAnimator.ofFloat(v, "scaleX", scale)
            val scaleYDown = ObjectAnimator.ofFloat(v, "scaleY", scale)
            scaleXDown.duration = 100
            scaleYDown.duration = 100

            val scaleXUp = ObjectAnimator.ofFloat(v, "scaleX", 1f)
            val scaleYUp = ObjectAnimator.ofFloat(v, "scaleY", 1f)
            scaleXUp.duration = 200
            scaleYUp.duration = 200

            val animatorSet = AnimatorSet()
            animatorSet.play(scaleXDown).with(scaleYDown).before(scaleXUp).before(scaleYUp)
            animatorSet.start()

            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onClick()
                }
            })
        }
    }
}