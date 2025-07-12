package com.example.basemvvm.common.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setVerticalLayout() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
}

fun RecyclerView.setHorizontalLayout() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
}

fun RecyclerView.setGridLayout(itemPerLine: Int) {
    layoutManager = GridLayoutManager(context, itemPerLine, LinearLayoutManager.VERTICAL, false)
}

fun RecyclerView.onScrollToLoadMoreListener(
    dy: Int,
    preloadBefore: Int = 3,
    onScrollEvent: () -> Unit
) {
    if (dy >= 0 && adapter != null && adapter!!.itemCount > 0 && layoutManager != null && layoutManager is LinearLayoutManager) {
        (layoutManager as? LinearLayoutManager)?.let {
            val totalItemCount = it.itemCount
            val lastVisibleItem = it.findLastVisibleItemPosition()
            if (totalItemCount > 0 && lastVisibleItem >= (totalItemCount - (preloadBefore + 1))) {
                onScrollEvent()
            }
        }
    }
}

fun RecyclerView.getLastVisibleItemIndex(): Int {
    val manager = (layoutManager as? LinearLayoutManager) ?: return -1
    return manager.findLastVisibleItemPosition()
}

fun RecyclerView.getFirstVisibleItemIndex(): Int {
    val manager = (layoutManager as? LinearLayoutManager) ?: return -1
    return manager.findFirstVisibleItemPosition()
}

fun RecyclerView.getFirstCompleteVisibleItemIndex(): Int {
    val manager = (layoutManager as? LinearLayoutManager) ?: return -1
    return manager.findFirstCompletelyVisibleItemPosition()
}

