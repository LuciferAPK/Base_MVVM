package com.example.basemvvm.ui.common.base

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.example.basemvvm.AppConfig
import com.example.basemvvm.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.basemvvm.ui.common.isLiving

abstract class BaseBottomSheetDialog<BINDING : ViewBinding> : BottomSheetDialogFragment() {
    private var mScreenDuration: Long = 0
    private var startScreenActiveTime: Long = 0
    protected fun getScreenDuration() = mScreenDuration

    private var binding: BINDING? = null

    fun viewBinding() = binding

    abstract fun makeBinding(inflater: LayoutInflater): BINDING

    abstract fun initViewAndData(saveInstanceState: Bundle?, binding: BINDING)

    override fun onAttach(context: Context) {
        super.onAttach(AppConfig.getLocalizedContext(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        if (savedInstanceState == null) onOpenScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = makeBinding(layoutInflater)
        if (this.dialog?.window != null) {
            this.dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        return binding!!.root
    }

    /**
     * Set size for dialog bottom sheet
     */
    fun setWidthForDialog(width: Int) {
        dialog?.window?.setLayout(
            (activity?.window?.decorView?.width ?: 0) * width / 10,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setHeightForDialog(height: Int) {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            height
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewAndData(savedInstanceState, binding!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun show(
        fragmentManager: FragmentManager,
        tag: String?
    ) {
        if (!fragmentManager.isLiving()) return
        runCatching {
            val transaction =
                fragmentManager.beginTransaction()
            val prevFragment = fragmentManager.findFragmentByTag(tag)
            if (prevFragment != null) {
                transaction.remove(prevFragment)
            }
            transaction.addToBackStack(null)
            show(transaction, tag)
        }
    }

    fun dismissDialog(tag: String?) {
        dismissAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        startScreenActiveTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        mScreenDuration += System.currentTimeMillis() - startScreenActiveTime
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isFinishing == true || isRemoving && activity?.isChangingConfigurations != true) {
            onScreenEndOfLifecycle()
        }
    }

    open fun onOpenScreen() {}

    open fun onScreenEndOfLifecycle() {}
}