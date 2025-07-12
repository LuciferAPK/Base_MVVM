package com.example.basemvvm.common.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.example.basemvvm.appContext
import com.example.basemvvm.data.enums.NetworkType

object NetworkUtils {

    fun openNetworkSetting(context: Context) {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    fun getSpeedType(context: Context): NetworkType {
        var type = NetworkType.MEDIUM
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities?.takeIf {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }?.let {
            val downloadSpeed = it.linkDownstreamBandwidthKbps
            type = when {
                downloadSpeed < 4000 -> NetworkType.SLOW
                downloadSpeed in 4000..15000 -> NetworkType.MEDIUM
                else -> NetworkType.FAST
            }
        }
        return type
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    fun isConnected(): Boolean {
        val connectivityManager =
            appContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE)
                )
    }
}