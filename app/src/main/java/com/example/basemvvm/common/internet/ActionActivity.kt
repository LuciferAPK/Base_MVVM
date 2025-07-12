package com.example.basemvvm.common.internet

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.basemvvm.R
import com.example.basemvvm.common.utils.gone
import com.example.basemvvm.common.utils.setSafeOnClickListener
import com.example.basemvvm.common.utils.visible

interface IListener {
    fun show()
    fun dismiss()
}

abstract class ActionActivity(var activity: Activity) : IListener {
    private val swipeToDismiss: Boolean = true
    var view: ViewGroup? = null
    private var isShowing = false
    private var isHiding = false
    private var y1 = 0f
    private var y2 = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun show() {
        y1 = 0f
        y2 = 0f
        if (isShowing) {
            return
        }
        isShowing = true
        if (view == null) {
            view = onCreateView()
            addView()
        }
        onViewCreated(view!!)
        view!!.setOnTouchListener(View.OnTouchListener { _, event ->
            if (swipeToDismiss) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        y1 = event.y
                        return@OnTouchListener checkDismiss()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        y2 = event.y
                        return@OnTouchListener checkDismiss()
                    }

                    MotionEvent.ACTION_UP -> {
                        y2 = event.y
                        return@OnTouchListener checkDismiss()
                    }
                }

                return@OnTouchListener true
            } else {
                return@OnTouchListener false
            }
        })
        view!!.setSafeOnClickListener {
            dismiss()
        }
        showView(view!!)
    }

    private fun checkDismiss(): Boolean {
        if (y1 - y2 > 50) {
            dismiss()
            return true
        }
        return false
    }

    private fun addView() {
        activity.window.addContentView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun dismiss() {
        if (isShowing && !isHiding) {
            onDismiss()
            view?.let {
                hideView(it)
            }
        }
    }

    private fun showView(view: View) {
        view.clearAnimation()
        view.visible()
        val animDown = AnimationUtils.loadAnimation(activity, R.anim.anim_move_down)
        view.startAnimation(animDown)
    }


    private fun hideView(view: ViewGroup) {
        view.clearAnimation()
        isHiding = true
        val animUp = AnimationUtils.loadAnimation(activity, R.anim.anim_move_up)
        view.gone()
        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                isHiding = false
                isShowing = false
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        view.startAnimation(animUp)
    }

    open fun onDismiss() {}

    abstract fun onCreateView(): ViewGroup

    abstract fun onViewCreated(view: ViewGroup)
}