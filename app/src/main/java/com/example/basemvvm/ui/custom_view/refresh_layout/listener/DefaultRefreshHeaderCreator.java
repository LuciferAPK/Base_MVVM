package com.example.basemvvm.ui.custom_view.refresh_layout.listener;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshHeader;
import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshLayout;

public interface DefaultRefreshHeaderCreator {
    @NonNull
    RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout);
}
