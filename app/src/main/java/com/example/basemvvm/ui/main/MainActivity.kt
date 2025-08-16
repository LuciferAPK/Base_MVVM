package com.example.basemvvm.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.basemvvm.databinding.ActivityMainBinding
import com.example.basemvvm.ui.NavigationManager
import com.example.basemvvm.ui.common.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private var job: Job? = null

    override fun makeBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initViewAndData(saveInstanceState: Bundle?, binding: ActivityMainBinding) {
//        if (saveInstanceState == null) AdHelper.resetFlags()
        if (viewModel.isNavigateToSplash) {
            job = lifecycleScope.launch {
                delay(1234)
                viewModel.isNavigateToSplash = false
            }
            NavigationManager.navigateToSplash(supportFragmentManager)
        } /*else NavigationManager.navigateToMain(supportFragmentManager)*/
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}