package com.example.basemvvm.common.utils

import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.experimental.and

val HEX_LOWERCASE =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

fun String.urlEncoder(): String {
    return try {
        URLEncoder.encode(this, "UTF-8").replace("+", "%20")
    } catch (e: UnsupportedEncodingException) {
        this
    }
}

inline fun <reified T> fromJson(json: String): T {
    return Gson().fromJson(json, object : TypeToken<T>() {}.type)
}

fun String.toHex(): String {
    val array = toByteArray()
    val var1 = CharArray(array.size shl 1)
    var var2 = 0

    array.forEach {
        val var4: Int = (it and 255.toByte()).toInt()
        var1[var2++] = HEX_LOWERCASE[var4 ushr 4]
        var1[var2++] = HEX_LOWERCASE[var4 and 15]
    }
    return String(var1)
}

val String.fromHtml: Spanned
    get() =
        Html.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)

fun String.fromHex(): String {
    val str = StringBuilder()
    var i = 0
    while (i < length) {
        str.append(substring(i, i + 2).toInt(16).toChar())
        i += 2
    }
    return str.toString()
}

fun String?.isNullOrEmptyOrBlank(): Boolean {
    return isNullOrEmpty() || isNullOrBlank()
}

fun String.toHex(pass: String): String {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(pass.toByteArray())
    val bytes = md.digest(this.toByteArray())
    val sb = StringBuilder()
    for (element in bytes) {
        sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
    }
    return sb.toString()
}

val Any.toJson: String
    get() {
        return try {
            Gson().toJson(this)
        } catch (e: Exception) {
            ""
        }
    }

fun String.validName(): String {
    val sanitized = replace(Regex("[<>:\"/|?*]"), "_")
    return sanitized.trim()
}
