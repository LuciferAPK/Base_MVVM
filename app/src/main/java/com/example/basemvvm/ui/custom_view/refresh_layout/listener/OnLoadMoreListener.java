package com.example.basemvvm.ui.custom_view.refresh_layout.listener;

import androidx.annotation.NonNull;

import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshLayout;

public interface OnLoadMoreListener {
    void onLoadMore(@NonNull RefreshLayout refreshLayout);
}
