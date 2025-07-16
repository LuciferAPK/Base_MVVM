package com.example.basemvvm.ui.custom_view.refresh_layout.listener;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshLayout;

public interface DefaultRefreshInitializer {
    void initialize(@NonNull Context context, @NonNull RefreshLayout layout);
}
