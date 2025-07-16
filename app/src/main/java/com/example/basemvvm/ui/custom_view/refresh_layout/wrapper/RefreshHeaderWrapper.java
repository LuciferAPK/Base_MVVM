package com.example.basemvvm.ui.custom_view.refresh_layout.wrapper;

import android.annotation.SuppressLint;
import android.view.View;

import com.example.basemvvm.ui.custom_view.refresh_layout.api.RefreshHeader;
import com.example.basemvvm.ui.custom_view.refresh_layout.simple.SimpleComponent;

@SuppressLint("ViewConstructor")
public class RefreshHeaderWrapper extends SimpleComponent implements RefreshHeader {

    public RefreshHeaderWrapper(View wrapper) {
        super(wrapper);
    }

}
