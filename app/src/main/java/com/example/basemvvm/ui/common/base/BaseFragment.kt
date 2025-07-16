package com.example.basemvvm.ui.common.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.basemvvm.AppConfig
import com.example.basemvvm.Logger
import com.example.basemvvm.common.EventHelper
import com.example.basemvvm.common.utils.NetworkUtils
import com.example.basemvvm.common.utils.setSafeOnClickListener
import com.example.basemvvm.common.utils.setSafeOnClickScaleEffect
import com.example.basemvvm.ui.common.isLiving

abstract class BaseFragment<BINDING : ViewBinding> : Fragment() {
    private var mScreenDuration: Long = 0
    private var startScreenActiveTime: Long = 0

    private var isUserScrolling = false
    private var lastFirstVisibleItem = 0
    var numberItemScrolled = 0

    open fun getRecyclerView(): RecyclerView? = null

    private fun getScreenDurationSession(): Long {
        if (startScreenActiveTime == 0L) return 0L
        return System.currentTimeMillis() - startScreenActiveTime
    }

    private fun getScreenDuration() = mScreenDuration

    fun getCurrentScreenDuration() = mScreenDuration + getScreenDurationSession()

    fun resetScreenDuration() {
        mScreenDuration = 0
        startScreenActiveTime = System.currentTimeMillis()
    }

    private var binding: BINDING? = null

    fun viewBinding() = binding

    abstract fun makeBinding(inflater: LayoutInflater): BINDING


    abstract fun initViewAndData(saveInstanceState: Bundle?, binding: BINDING)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) onOpenScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = makeBinding(inflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewAndData(savedInstanceState, binding!!)
        EventHelper.register(this)
    }

    override fun onDestroyView() {
        EventHelper.unregister(this)
        binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        startScreenActiveTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        mScreenDuration += getScreenDurationSession()
        startScreenActiveTime = System.currentTimeMillis()
    }


    override fun onAttach(context: Context) {
        super.onAttach(AppConfig.getLocalizedContext(context))
    }

    override fun onDestroy() {
        super.onDestroy()
        val isParentRemoving = parentFragment?.isRemoving == true
        if (activity?.isFinishing == true || (isRemoving || isParentRemoving) && activity?.isChangingConfigurations != true) {
            onScreenEndOfLifecycle()
        }
    }

    open fun onOpenScreen() {
        Logger.logAction("openScreen:${this.javaClass.simpleName}")
    }

    open fun onScreenEndOfLifecycle() {
        Logger.logAction("stopScreen:${this.javaClass.simpleName}")
    }

    fun View.setSafeInternetOnClick(onSafeClick: (View) -> Unit) {
        setSafeOnClickListener {
            if (!NetworkUtils.isConnected()) {
                (activity as? BaseActivity<*>)?.handleShowDialogInternet()
            } else onSafeClick.invoke(this)
        }
    }

    fun View.setSafeInternetScaleEffectOnClick(onSafeClick: (View) -> Unit) {
        setSafeOnClickScaleEffect {
            if (!isLiving()) return@setSafeOnClickScaleEffect
            if (!NetworkUtils.isConnected()) {
                (activity as? BaseActivity<*>)?.handleShowDialogInternet()
            } else onSafeClick.invoke(this)
        }
    }
}