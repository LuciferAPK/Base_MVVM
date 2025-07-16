package com.example.basemvvm.ui.custom_view.refresh_layout.api;

import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;

import com.example.basemvvm.ui.custom_view.refresh_layout.listener.ScrollBoundaryDecider;

public interface RefreshContent {

    @NonNull
    View getView();

    @NonNull
    View getScrollableView();

    void onActionDown(MotionEvent e);

    void setUpComponent(RefreshKernel kernel, View fixedHeader, View fixedFooter);

    void setScrollBoundaryDecider(ScrollBoundaryDecider boundary);

    void setEnableLoadMoreWhenContentNotFull(boolean enable);

    void moveSpinner(int spinner, int headerTranslationViewId, int footerTranslationViewId);

    boolean canRefresh();

    boolean canLoadMore();

    AnimatorUpdateListener scrollContentWhenFinished(int spinner);
}
