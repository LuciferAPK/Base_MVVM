package com.example.basemvvm.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import com.example.basemvvm.common.utils.setSafeOnClickListener
import com.example.basemvvm.databinding.NoInternetDialogBinding
import com.example.basemvvm.ui.common.base.BaseDialog

class NoInternetDialog : BaseDialog<NoInternetDialogBinding>() {

    private var onClickBack: (() -> Unit)? = null
    private var onClickViewSettings: (() -> Unit)? = null

    fun setOnClickBack(onClickBack: () -> Unit) {
        this.onClickBack = onClickBack
    }

    fun setOnClickViewSettings(onClickViewSettings: () -> Unit) {
        this.onClickViewSettings = onClickViewSettings
    }

    override fun makeBinding(inflater: LayoutInflater): NoInternetDialogBinding {
        return NoInternetDialogBinding.inflate(inflater)
    }

    override fun getGravityForDialog(): Int {
        return Gravity.BOTTOM
    }

    override fun initViewAndData(
        saveInstanceState: Bundle?,
        binding: NoInternetDialogBinding
    ) {
        this.isCancelable = true
        binding.buttonNetworkSetting.setSafeOnClickListener {
            onClickViewSettings?.invoke()
            dismissDialog(TAG)
        }
    }

    companion object {
        const val TAG = "NoInternetDialog"
    }
}