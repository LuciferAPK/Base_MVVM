package com.example.basemvvm.ui.common

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.basemvvm.common.utils.fadeAndScaleDownDynamic
import com.example.basemvvm.common.utils.fadeAndScaleUpDynamic
import com.example.basemvvm.common.utils.getFirstVisibleItemIndex
import com.example.basemvvm.common.utils.invisible
import com.example.basemvvm.common.utils.postDelayedSkipException
import com.example.basemvvm.common.utils.setSafeOnClickListener
import com.example.basemvvm.common.utils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollAndJumpTopHelper(
    private val lifecycleOwner: LifecycleOwner,
    private var mRecyclerView: RecyclerView?,
    private var mJumpTopButton: View?,
    private val hideItemIndex: Int = 6,
    private var mListener: Listener?
) : DefaultLifecycleObserver {
    private var isScrollingToTop = false
    private var showJumpTopJob: Job? = null
    private var mJumpTopShowing = false

    private fun isListNotEmpty() =
        mRecyclerView != null && mRecyclerView!!.adapter != null && mRecyclerView!!.adapter!!.itemCount > 0

    private val mRecyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            if (!isScrolling) {
                isScrollingToTop = false
            }
            if (isListNotEmpty())
                mListener?.onScrollingStateChange(isScrolling)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (isScrollingToTop) return
            if (isListNotEmpty())
                mListener?.onRecyclerViewScrolled(dy)
            postDelayedSkipException {
                handleJumpTopButtonVisibleState(dy)
            }
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        mJumpTopButton?.setSafeOnClickListener {
            handleJumpTopOnClick()
            mListener?.onJumpTopClick()
        }
        try {
            mRecyclerView?.removeOnScrollListener(mRecyclerScrollListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mRecyclerView?.addOnScrollListener(mRecyclerScrollListener)
    }

    interface Listener {
        fun onJumpTopClick() {}
        fun onScrollingStateChange(isScrolling: Boolean) {}
        fun onRecyclerViewScrolled(dy: Int) {}
    }


    private fun handleJumpTopButtonVisibleState(dy: Int) {
        fun hideJumpTop() {
            showJumpTopJob?.cancel()
            showJumpTopJob = null
            if (mJumpTopShowing) {
                mJumpTopShowing = false
                mJumpTopButton?.run {
                    fadeAndScaleDownDynamic { invisible() }
                }
            }
        }

        fun showJumpTopDelayed() {
            showJumpTopJob = lifecycleOwner.lifecycleScope.launch {
                delay(250)
                if (!mJumpTopShowing) {
                    mJumpTopShowing = true
                    mJumpTopButton?.run {
                        visible()
                        fadeAndScaleUpDynamic()
                    }
                }
            }
        }
        if (dy < 0 && mRecyclerView?.layoutManager != null) {
            val lastItemIndex =
                (mRecyclerView?.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            if (lastItemIndex < hideItemIndex) {
                hideJumpTop()
            } else if (showJumpTopJob == null) {
                showJumpTopDelayed()
            }
        } else {
            hideJumpTop()
        }
    }

    private fun handleJumpTopOnClick() {
        if (mRecyclerView == null) return
        isScrollingToTop = true
        if (mJumpTopShowing) {
            mJumpTopShowing = false
            mJumpTopButton?.run {
                fadeAndScaleDownDynamic { invisible() }
            }
        }
        showJumpTopJob?.cancel()
        showJumpTopJob = null
        if (mRecyclerView!!.getFirstVisibleItemIndex() <= 3)
            smoothScrollToTopSort()
        else mRecyclerView?.smoothScrollToPosition(0)
    }

    private fun smoothScrollToTopSort() {
        if (mRecyclerView == null) return
        val smoothScroller = object : LinearSmoothScroller(mRecyclerView!!.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                var time = super.calculateTimeForScrolling(dx)
                if (time < 150) time = 150
                return time
            }
        }
        smoothScroller.targetPosition = 0
        (mRecyclerView?.layoutManager as LinearLayoutManager).startSmoothScroll(
            smoothScroller
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        runCatching {
            mRecyclerView?.removeOnScrollListener(mRecyclerScrollListener)
            mRecyclerView = null
            mJumpTopButton = null
            mListener = null
            lifecycleOwner.lifecycle.removeObserver(this)
        }
        super.onDestroy(owner)
    }
}