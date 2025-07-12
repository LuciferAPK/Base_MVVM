package com.example.basemvvm.config

object UrlBuilder {
    enum class Type { PHOTO, VIDEO }

    fun getUrl(): String {
        return "fullUrl"
    }
}