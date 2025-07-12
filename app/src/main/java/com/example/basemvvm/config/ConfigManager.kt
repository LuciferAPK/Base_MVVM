package com.example.basemvvm.config

import com.example.basemvvm.Constants
import com.example.basemvvm.Logger
import com.example.basemvvm.appContext
import com.example.basemvvm.common.utils.NetworkUtils
import com.example.basemvvm.common.utils.backgroundLaunchSafe
import com.example.basemvvm.data.enums.NetworkType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConfigManager {
    companion object {
        /**Adjust*/
        var deviceID: String? = null
        var network: String? = null
        var campaign: String? = null

        var country: String = "OT"
        var city: String = "none"
        var networkType: NetworkType = NetworkType.MEDIUM
    }

    private suspend fun getCountryInfo(): Pair<String, String> {
        val startTime = System.currentTimeMillis()
        val (mCountry, mCity, countryType) = CountryCode().handleProcessCountryCode()
        country = mCountry.uppercase()
        city = mCity
        return Pair(country, city)
    }

    suspend fun handleGetConfigInApp(isFirstOpen: Boolean) {
        val (mCountry, city) = getCountryInfo()
        Logger.logAction("APP_ID:${Constants.APP_ID}")
        Logger.logAction("Country Info: $mCountry city:$city")
        Logger.logAction("==>Start get config<===")
        runCatching {
//            Firebase.remoteConfig.fetch().addOnCompleteListener {
//                backgroundLaunchSafe {
//                    if (it.isSuccessful) Firebase.remoteConfig.activate()
//                    setConfigVariables()
//                }
//            }
        }
        networkType = NetworkUtils.getSpeedType(appContext())
        val jsonConfig = GetJsonConfigFlow().getConfig(isFirstOpen, mCountry)
        Logger.logAction("JsonConfig Remote:${jsonConfig}")
        ServerUrlManager.setServerBaseUrl(jsonConfig, mCountry)
        StorageManager.setStorageBaseUrl(jsonConfig, mCountry)
        FirebaseStorageManager.setStorageUrl(appContext(), mCountry)
        setConfigVariables()
    }

    private suspend fun setConfigVariables() {
//        abOffDemo =
//            RemoteConfigByKey.getBoolean(RemoteConfigByKey.ConfigKey.AB_OFF_DEMO)
//        Logger.logAction("abOffDemo: $abOffDemo")
    }
}