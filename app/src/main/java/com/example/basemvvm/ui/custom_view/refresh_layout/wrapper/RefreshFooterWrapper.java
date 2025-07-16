package com.example.basemvvm.ui.custom_view.refresh_layout.wrapper;

import android.annotation.SuppressLint;
import android.view.View;

import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshFooter;
import com.example.basemvvm.ui.custom_view.refresh_layout.simple.SimpleComponent;

@SuppressLint("ViewConstructor")
public class RefreshFooterWrapper extends SimpleComponent implements RefreshFooter {

    public RefreshFooterWrapper(View wrapper) {
        super(wrapper);
    }

}
