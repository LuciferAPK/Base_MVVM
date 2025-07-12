package com.example.basemvvm.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val defaultValueLong: Long = -1
    private val defaultValueInteger = -1
    private val defaultValueFloat = -1

    private val mPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun save(key: String?, value: Boolean) {
        mPrefs.edit().putBoolean(key, value).apply()
    }

    @Synchronized
    fun save(key: String?, value: String?) {
        mPrefs.edit().putString(key, value).apply()
    }

    @Synchronized
    fun save(key: String?, value: Float) {
        mPrefs.edit().putFloat(key, value).apply()
    }

    @Synchronized
    fun save(key: String?, value: Int) {
        mPrefs.edit().putInt(key, value).apply()
    }

    @Synchronized
    fun save(key: String?, value: Long) {
        mPrefs.edit().putLong(key, value).apply()
    }

    @Synchronized
    fun getBoolean(key: String?): Boolean {
        return mPrefs.getBoolean(key, false)
    }

    @Synchronized
    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return mPrefs.getBoolean(key, defaultValue)
    }

    @Synchronized
    fun getString(key: String?, defaultValue: String? = null): String? {
        return mPrefs.getString(key, defaultValue)
    }

    @Synchronized
    fun getLong(key: String?): Long {
        return mPrefs.getLong(
            key, defaultValueLong
        )
    }

    @Synchronized
    fun getInt(key: String?): Int {
        return mPrefs.getInt(
            key, defaultValueInteger
        )
    }

    @Synchronized
    fun getInt(key: String?, defaultValue: Int): Int {
        return mPrefs.getInt(
            key, defaultValue
        )
    }

    @Synchronized
    fun getFloat(key: String?): Float {
        return mPrefs.getFloat(
            key, defaultValueFloat.toFloat()
        )
    }

    fun remove(key: String?) {
        mPrefs.edit().remove(key).apply()
    }
}