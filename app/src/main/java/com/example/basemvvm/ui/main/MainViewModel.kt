package com.example.basemvvm.ui.main

import androidx.lifecycle.ViewModel
import com.example.basemvvm.data.local.PreferencesManager
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: Lazy<PreferencesManager>
) : ViewModel() {
    var isNavigateToSplash = true
}