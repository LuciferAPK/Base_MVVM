package com.example.basemvvm.ui.main.onboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basemvvm.Logger
import com.example.basemvvm.appRepository
import com.example.basemvvm.common.crashlytic.CrashlyticsHelper
import com.example.basemvvm.common.utils.runInBackground
import com.example.basemvvm.config.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    fun isFirstOpenApp() = appRepository().isFirstOpenApp()

    val configStatus = MutableLiveData<Boolean?>(null)
    fun getAppRemoteConfig() {
        if (configStatus.value == true) {
            configStatus.value = true
            return
        }
        viewModelScope.launch {
            try {
                runInBackground {
                    appRepository().checkFirstOpenAppStatus()
                    ConfigManager().handleGetConfigInApp(appRepository().isFirstOpenApp())
                }
                configStatus.value = true
            } catch (e: Exception) {
                Logger.logAction("Error:${e.message}")
                e.printStackTrace()
                CrashlyticsHelper.recordException(e, "GetConfig Flow Error")
                configStatus.value = false
            }
        }
    }
}