package com.example.basemvvm.ui.common.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.basemvvm.AppConfig
import com.example.basemvvm.Logger
import com.example.basemvvm.R
import com.example.basemvvm.common.EventHelper
import com.example.basemvvm.common.display.DisplayManager
import com.example.basemvvm.common.image.ImageLoader
import com.example.basemvvm.common.internet.AnnounceConnectionState
import com.example.basemvvm.common.utils.MyInternet
import com.example.basemvvm.common.utils.NetworkUtils
import com.example.basemvvm.common.utils.myEnableEdgeToEdge
import com.example.basemvvm.common.utils.setSafeOnClickListener
import com.example.basemvvm.common.utils.setSafeOnClickScaleEffect
import com.example.basemvvm.ui.common.isLiving
import com.example.basemvvm.ui.dialog.NoInternetDialog
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<BINDING : ViewBinding> : AppCompatActivity() {
    private var mScreenDuration: Long = 0
    private var startScreenActiveTime: Long = 0
    protected fun getScreenDuration() = mScreenDuration
    private fun getScreenDurationSession(): Long {
        if (startScreenActiveTime == 0L) return 0L
        return System.currentTimeMillis() - startScreenActiveTime
    }

    fun getCurrentScreenDuration() = mScreenDuration + getScreenDurationSession()

    fun resetScreenDuration() {
        mScreenDuration = 0
        startScreenActiveTime = System.currentTimeMillis()
    }

    private var binding: BINDING? = null

    fun viewBinding() = binding

    abstract fun makeBinding(inflater: LayoutInflater): BINDING

    abstract fun initViewAndData(saveInstanceState: Bundle?, binding: BINDING)

    open fun handleBackPress() = false

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            doFinish()
        }
    }

    open fun doFinish() {
        finish()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = makeBinding(layoutInflater)
        setContentView(binding!!.root)
        initViewAndData(savedInstanceState, binding!!)
        runCatching { observerInternetConnection() }
        if (savedInstanceState == null) onOpenScreen()
        EventHelper.register(this)
    }

    open fun onOpenScreen() {}

    override fun onResume() {
        super.onResume()
        myEnableEdgeToEdge(lightStatusBar = true)
        startScreenActiveTime = System.currentTimeMillis()
        if (handleBackPress()) onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        mScreenDuration += getScreenDurationSession()
        startScreenActiveTime = System.currentTimeMillis()
        if (handleBackPress()) backPressedCallback.remove()
    }

    override fun onDestroy() {
        EventHelper.unregister(this)
        binding = null
        super.onDestroy()
        if (isFinishing) {
            onScreenEndOfLifecycle()
        }
    }

    open fun onScreenEndOfLifecycle() {
        Logger.logAction("activity stopScreen:${this.javaClass.simpleName}")
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.clearCache()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppConfig.getLocalizedContext(newBase))
    }

    private var notifyConnection: AnnounceConnectionState? = null

    private val noInternetDialog by lazy { NoInternetDialog() }

    private fun observerInternetConnection() {
        if (!isLiving()) return
        MyInternet(this).observe(this) { isConnected ->
            isConnected?.let {
                if (it) {
                    if (noInternetDialog.isAdded) {
                        showSnackBarInternetConnected(binding!!.root)
                        noInternetDialog.dismissDialog(NoInternetDialog.TAG)
                    }
                } else handleShowDialogInternet()
            }
        }
    }

    fun handleShowDialogInternet() {
        if (!isLiving()) return
        val existDialog = supportFragmentManager.findFragmentByTag(NoInternetDialog.TAG)
        if (noInternetDialog.isAdded && existDialog != null) return

        noInternetDialog.setOnClickViewSettings {
            NetworkUtils.openNetworkSetting(this@BaseActivity)
        }
        noInternetDialog.setOnClickBack {
            EventHelper.post(EventGoToMyFiles())
        }
        runCatching {
            noInternetDialog.show(supportFragmentManager, NoInternetDialog.TAG)
        }
    }

    class EventGoToMyFiles

    @SuppressLint("RestrictedApi")
    private fun showSnackBarInternetConnected(view: View) {
        val snackBar = Snackbar.make(view, "", 3000)
        val customSnackView =
            LayoutInflater.from(this).inflate(R.layout.layout_toast_internet_connected, null)
        val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
        snackBar.view.setBackgroundResource(R.drawable.bg_toast_internet_connected)
        snackBar.view.translationY = -(DisplayManager.dpToPx(this, 50f).toFloat())
        val layoutParams = snackBarLayout.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = DisplayManager.dpToPx(this, 16f)
        layoutParams.marginEnd = DisplayManager.dpToPx(this, 16f)
        snackBarLayout.layoutParams = layoutParams
        snackBarLayout.addView(customSnackView, 0)
        snackBar.show()
    }

    fun View.setSafeInternetOnClick(onSafeClick: (View) -> Unit) {
        setSafeOnClickListener {
            if (!isLiving()) return@setSafeOnClickListener
            if (!NetworkUtils.isConnected()) {
                handleShowDialogInternet()
            } else onSafeClick.invoke(this)
        }
    }

    fun View.setSafeInternetScaleEffectOnClick(onSafeClick: (View) -> Unit) {
        setSafeOnClickScaleEffect {
            if (!isLiving()) return@setSafeOnClickScaleEffect
            if (!NetworkUtils.isConnected()) {
                handleShowDialogInternet()
            } else onSafeClick.invoke(this)
        }
    }
}
