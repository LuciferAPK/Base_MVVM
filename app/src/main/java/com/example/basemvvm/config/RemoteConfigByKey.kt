package com.example.basemvvm.config

import com.example.basemvvm.common.crashlytic.CrashlyticsHelper

object RemoteConfigByKey {
    enum class ConfigKey(val value: String) {
        OFF_AD_VERSION_CODE("offAdVersionCode"),
    }

    suspend fun getInt(configKey: ConfigKey, defaultValue: Int = 0) =
        getRemoteConfigByKey(configKey)?.toIntOrNull() ?: defaultValue

    suspend fun getBoolean(configKey: ConfigKey, defaultValue: Boolean = true) =
        getRemoteConfigByKey(configKey)?.toBooleanStrictOrNull() ?: defaultValue

    suspend fun getString(configKey: ConfigKey) = getRemoteConfigByKey(configKey)

    private fun getRemoteConfigByKey(key: ConfigKey): String? =
        try {
            "Firebase.remoteConfig.getValue(key.value).asString()"
        } catch (e: Exception) {
            CrashlyticsHelper.recordException(e)
            e.printStackTrace()
            null
        }
}