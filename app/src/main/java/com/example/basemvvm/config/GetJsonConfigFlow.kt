package com.example.basemvvm.config

import com.example.basemvvm.BuildConfig
import com.example.basemvvm.Logger
import com.example.basemvvm.appContext
import com.example.basemvvm.common.crashlytic.CrashlyticsHelper
import com.example.basemvvm.common.utils.NetworkUtils
import com.example.basemvvm.data.response.RemoteConfigResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withTimeoutOrNull

class GetJsonConfigFlow {
    private val TAG_APPID = "APPID_CONFIG"

    suspend fun getConfig(isFirstOpen: Boolean, country: String): RemoteConfigResponse {
        setFirebaseConfig(isFirstOpen)
        val localConfig = getRemoteConfigDefault()
        Logger.logAction("ConfigLocal:${localConfig}")
        try {
            var result: RemoteConfigResponse? = withTimeoutOrNull(7000) {
                fetchRemoteConfig()
            }
//            if (result == null) {
//                result = withTimeoutOrNull(3000) {
//                    getConFigHosting(localConfig?.firebaseHosting)
//                }
//            }
            if (result == null) {
                result = withTimeoutOrNull(3000) {
                    getConFigS3Amazon(country, localConfig)
                }
            }
            if (result == null) {
                result = withTimeoutOrNull(3000) {
                    retryGetRemoteConfig(country, localConfig)
                }
            }
            if (result == null) {
                result = getConfigLocal()
            }
            if (result == null) {

            }
            return result ?: localConfig!!
        } catch (e: Exception) {
            e.printStackTrace()
            return localConfig!!
        }
    }

    private fun setFirebaseConfig(isFirstOpen: Boolean) {
        val interval = if (isFirstOpen) 0L else if (BuildConfig.DEBUG) 20L else 3600L
//        Firebase.remoteConfig.setConfigSettingsAsync(
//            FirebaseRemoteConfigSettzings.Builder()
//                .setMinimumFetchIntervalInSeconds(interval)
//                .build()
//        )
    }

    private fun getRemoteKey() = "configs_" + appContext().packageName.replace(".", "_")

    private suspend fun fetchRemoteConfig(): RemoteConfigResponse? {
        try {
            /**pending*/
            return null
//            val config = Firebase.remoteConfig
//            if (NetworkUtils.isConnected())
//                config.fetchAndActivate().await()
//            val json = config.getString(getRemoteKey())
//            if (!checkValidateConfig(json)) return null
//            return fetchDone(json, typeConfig = LoadConfigEvent.ConfigType.FB_REMOTE)
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsHelper.recordException(e)
            return null
        }
    }

    private suspend fun getConFigHosting(url: String?): RemoteConfigResponse? {
        /**pending*/
        return null
//        if (!NetworkUtils.isConnected()) return null
//        val validUrl = url?.replace(TAG_APPID, Constants.APP_ID) ?: return null
//        try {
//            val requestConfig = appRepository().getRemoteConfig(validUrl)
//            if (!checkValidateConfig(requestConfig)) return null
//            return fetchDone(
//                remoteConfig = requestConfig,
//                typeConfig = LoadConfigEvent.ConfigType.FB_HOSTING
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            CrashlyticsHelper.recordException(e)
//            return null
//        }
    }

    private suspend fun getConFigS3Amazon(
        country: String,
        localConfig: RemoteConfigResponse? = null
    ): RemoteConfigResponse? {
        /**Pending*/
        return null
//        if (!NetworkUtils.isConnected()) return null
//        fun getUrlS3Amazon(country: String): String? {
//            if (ServerRegion.EU.value.contains(country) && !localConfig?.s3Eu.isNullOrEmpty())
//                return localConfig!!.s3Eu
//            if (ServerRegion.AS.value.contains(country) && !localConfig?.s3As.isNullOrEmpty())
//                return localConfig!!.s3As
//            return localConfig?.s3Us
//        }
//
//        val urlS3 =
//            getUrlS3Amazon(country)?.replace(TAG_APPID, Constants.APP_ID) ?: return null
//
//        try {
//            val requestConfig = appRepository().getRemoteConfig(urlS3)
//            if (!checkValidateConfig(requestConfig)) return null
//            return fetchDone(
//                remoteConfig = requestConfig,
//                typeConfig = LoadConfigEvent.ConfigType.AMAZON
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            CrashlyticsHelper.recordException(e)
//            return null
//        }
    }

    private suspend fun retryGetRemoteConfig(
        country: String,
        localConfig: RemoteConfigResponse? = null
    ): RemoteConfigResponse? {
        /**pending*/
        return null
//        if (!NetworkUtils.isConnected()) return null
//        try {
//            val defaultConfig = localConfig?.toLocalMap() ?: return null
//            val config = Firebase.remoteConfig
//            config.reset().await()
//            config.setDefaultsAsync(defaultConfig).await()
//            config.fetchAndActivate().await()
//            val json = config.getString(getRemoteKey())
//            if (checkValidateConfig(json)) {
//                return fetchDone(
//                    json,
//                    typeConfig = LoadConfigEvent.ConfigType.FB_REMOTE_RETRY
//                )
//            }
//            return fetchDone(json, typeConfig = LoadConfigEvent.ConfigType.DEFAULT)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            CrashlyticsHelper.recordException(e)
//        }
//        return null
    }

    private fun checkValidateConfig(configInfo: String?): Boolean {
        return !configInfo.isNullOrEmpty() && configInfo != "{}"
    }

    private fun checkValidateConfig(remoteConfig: RemoteConfigResponse?): Boolean {
        return remoteConfig != null
    }

    private fun getRemoteConfigDefault(): RemoteConfigResponse? {
        /**pending*/
        return null
//        val configLocal = SecurityToken.getConfig()
//        if (!checkValidateConfig(configLocal)) return null
//        try {
//            val type = object : TypeToken<RemoteConfigResponse>() {}.type
//            return Gson().fromJson(configLocal, type)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            CrashlyticsHelper.recordException(e)
//        }
//        return null
    }

    private fun getConfigLocal(): RemoteConfigResponse? {
        /**pending*/
        return null
//        val configLocal = SecurityToken.getConfig()
//        if (!checkValidateConfig(configLocal)) return null
//        return fetchDone(configLocal, typeConfig = LoadConfigEvent.ConfigType.FILE)
    }

//    private fun fetchDone(
//        response: String? = null,
//        remoteConfig: RemoteConfigResponse? = null,
//        typeConfig: LoadConfigEvent.ConfigType
//    ): RemoteConfigResponse? {
//        try {
//            val type = object : TypeToken<RemoteConfigResponse>() {}.type
//            val configResult =
//                remoteConfig ?: Gson().fromJson(response ?: return null, type)
//            Logger.logAction("getConfig done from $typeConfig value= $configResult")
//            return configResult
//        } catch (e: Exception) {
//            CrashlyticsHelper.recordException(e)
//            e.printStackTrace()
//            Logger.logAction(" RemoteConfig not Valid!")
//        }
//        return null
//    }
}