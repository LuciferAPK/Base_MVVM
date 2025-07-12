package com.example.basemvvm.common.internet

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup

class AnnounceConnectionState(activity: Activity, private var onDismiss: (() -> Unit)?) :
    ActionActivity(activity) {

    private var duration: Long = 5000
    private var dismissHandler: Handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable = Runnable {
        dismiss()
    }

//    private var binding: NoInternetDialogBinding? = null
    private var binding: ViewGroup? = null

    override fun onCreateView(): ViewGroup {
//        binding = NoInternetDialogBinding.inflate(LayoutInflater.from(activity))
        return binding!!/*binding!!.root*/
    }

    override fun onViewCreated(view: ViewGroup) {
        dismissHandler.postDelayed(dismissRunnable, duration)
    }

    override fun onDismiss() {
//        binding = null
        onDismiss?.invoke()
        dismissHandler.removeCallbacks(dismissRunnable)
    }

    fun dismissDialog() {
        dismiss()
    }
}