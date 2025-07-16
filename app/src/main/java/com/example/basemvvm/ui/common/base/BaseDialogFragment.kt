package com.example.basemvvm.ui.common.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.basemvvm.AppConfig
import com.example.basemvvm.ui.common.isLiving

abstract class BaseDialogFragment : DialogFragment() {
    private var mScreenDuration: Long = 0
    private var startScreenActiveTime: Long = 0
    protected fun getScreenDuration() = mScreenDuration

    open var canceledOnTouchOutside: Boolean = false
    open var dim: Float = 0.4f
    protected var mDialog: Dialog? = null
    protected abstract val layoutId: Int
    var mView: View? = null

    override fun onAttach(context: Context) {
        super.onAttach(AppConfig.getLocalizedContext(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) onOpenScreen()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BaseDialog(requireContext(), theme)
        try {
            dialog.onBackListener = this::onBackPressed
            dialog.touchOutside = canceledOnTouchOutside
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val mView = dialog.layoutInflater.inflate(layoutId, null)
            dialog.setContentView(mView)
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            val window = dialog.window!!
            window.setGravity(Gravity.CENTER)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(dim)
            this.mView = mView
            mDialog = dialog
            init(mView)
            refreshLayout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dialog
    }

    override fun onDetach() {
        mDialog = null
        super.onDetach()
        mView = null
    }

    fun setLayout(width: Int, height: Int) {
        mDialog?.window?.setLayout(width, height)
    }

    protected open fun init(view: View) {

    }

    open fun onBackPressed(): Boolean {
        return true
    }

    open fun refreshLayout() {

    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (!manager.isLiving()) return
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            runCatching {
                val ft = manager.beginTransaction()
                ft.add(this, tag)
                ft.commitAllowingStateLoss()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {}

    private class BaseDialog : Dialog {

        var onBackListener: (() -> Boolean)? = null
        var touchOutside: Boolean = false

        constructor(context: Context) : super(context)
        constructor(context: Context, themeResId: Int) : super(context, themeResId)
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
