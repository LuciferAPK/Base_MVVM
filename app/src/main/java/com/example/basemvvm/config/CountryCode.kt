package com.example.basemvvm.config

import com.example.basemvvm.Logger
import com.example.basemvvm.appRepository
import com.example.basemvvm.common.utils.NetworkUtils
import com.example.basemvvm.data.enums.CountryType
import com.example.basemvvm.data.response.CountryResponse
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class CountryCode {
    companion object {
        const val COUNTRY_CODE_URL = "https://ipinfo.io/json"
        const val COUNTRY_CODE_URL_RETRY = "https://api.infoip.io"
    }

    private val TAG = "CountryCode"
    private val language_country_list = listOf(
        *"az_AZ,bg_BG,cs_CZ,da_DK,de_DE,el_GR,fa_IR,fi_FI,hr_HR,hu_HU,in_ID,it_IT,iw_IL,ja_JP,ko_KR,lt_LT,lv_LV,mr_IN,ms_MY,nl_NL,ro_RO,ru_RU,sk_SK,sr_RS,sv_SE,th_TH,tr_TR,uk_UA,vi_VN,zh_TW,cn_TW,hk_TW"
            .split(",".toRegex()).toTypedArray()
    )

    suspend fun handleProcessCountryCode(): Triple<String, String, String> {
        var newCountry = appRepository().getCountryCode() ?: "OT"
        var city = appRepository().getCity() ?: "none"
        val deviceLanguage = Locale.getDefault().language.lowercase(Locale.ENGLISH)
        val deviceCountry = Locale.getDefault().country.uppercase(Locale.ENGLISH)
        fun logInfo(saved: Boolean = false) {
            Logger.logAction("Get Country info: $newCountry city:$city  device:${deviceLanguage}_$deviceCountry saved:$saved")
        }
        // Kiểm tra country đã luu trong máy
        if (isNotOT(newCountry)) return Triple(newCountry, city, CountryType.OFFLINE.value).apply { logInfo(true) }

        for (langCountry in language_country_list) {
            if (langCountry.startsWith(deviceLanguage)) {
                newCountry = langCountry.replace("${deviceLanguage}_", "")
                break
            }
        }
        // Kiểm tra country trong language_country_list
        if (isNotOT(newCountry)) {
            return Triple(newCountry, city, CountryType.LANGUAGE.value).apply {
                appRepository().saveCountryCity(city, newCountry)
                logInfo(true)
            }
        }

        if (!NetworkUtils.isConnected()) {
            newCountry = deviceCountry
            return Triple(newCountry, city, CountryType.FAILED.value).apply { logInfo() }
        }

        val res = withTimeoutOrNull(3000) { fetchCountry() }
            ?: withTimeoutOrNull(3000) { fetchCountry(true) }
            ?: return Triple(newCountry, city, CountryType.FAILED.value).apply { logInfo() }

        newCountry = (res.country ?: res.countryShort ?: "OT").uppercase(Locale.ENGLISH)
        city = res.city ?: "none"

        if (isNotOT(newCountry)) {
            val cType =
                if (res.countryShort?.isNotEmpty() == true) CountryType.TP_SERVER else CountryType.IP_INFO
            return Triple(newCountry, city, cType.value).apply {
                appRepository().saveCountryCity(city, newCountry)
                logInfo(true)
            }
        }

        return Triple(deviceCountry, city, CountryType.FAILED.value).apply { logInfo() }

    }

    private suspend fun fetchCountry(isRetryApi: Boolean = false): CountryResponse? {
        val url =
            if (isRetryApi) COUNTRY_CODE_URL_RETRY else COUNTRY_CODE_URL
        try {
            return appRepository().fetCountryCode(url)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(TAG, "fetchCountry error: $e")
        }
        return null
    }

    private fun isNotOT(country: String?): Boolean {
        return !country.isNullOrEmpty() && country.uppercase() != "OT"
    }
}