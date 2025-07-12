package com.example.basemvvm.data.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.basemvvm.Logger
import com.example.basemvvm.appContext
import com.example.basemvvm.common.utils.FileUtils
import com.example.basemvvm.common.utils.backgroundLaunch
import com.example.basemvvm.common.utils.runInIO
import com.example.basemvvm.config.ConfigManager
import com.example.basemvvm.data.local.PreferencesKey
import com.example.basemvvm.data.local.PreferencesManager
import com.example.basemvvm.data.network.Api
import com.example.basemvvm.data.network.DownloadApi
import com.example.basemvvm.data.response.CountryResponse
import com.example.basemvvm.data.response.RemoteConfigResponse
import dagger.Lazy
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val api: Api,
    private val apiDownload: DownloadApi,
    private val preferencesManager: Lazy<PreferencesManager>,
//    private val appDatabase: Lazy<AppDatabase>
) {
    private var mIsFirstOpenApp = true

    fun isFirstOpenApp() = mIsFirstOpenApp

    fun setFirstOpenAppToFalse() {
        backgroundLaunch {
            preferencesManager.get().save(PreferencesKey.IS_FIRST_TIME_OPEN_APP, false)
        }
    }

    fun checkFirstOpenAppStatus() {
        mIsFirstOpenApp =
            preferencesManager.get().getBoolean(PreferencesKey.IS_FIRST_TIME_OPEN_APP, true)
    }

    fun getLanguageSet(): String = preferencesManager.get().getString(PreferencesKey.KEY_LANGUAGE)
        ?: Locale.getDefault().language

    fun getCountryCode() =
        preferencesManager.get().getString(PreferencesKey.COUNTRY_CODE) ?: ConfigManager.country

    fun getCity() =
        preferencesManager.get().getString(PreferencesKey.CITY_CODE) ?: ConfigManager.city

    fun isCountryVN() = getCountryCode() == "VN"

    fun saveCountryCity(city: String, country: String) {
        preferencesManager.get().save(PreferencesKey.COUNTRY_CODE, country)
        preferencesManager.get().save(PreferencesKey.CITY_CODE, city)
    }

    suspend fun fetCountryCode(url: String): CountryResponse {
        return runInIO {
            api.getCountryCode(url, Locale.getDefault().language)
        }
    }

    suspend fun getRemoteConfig(url: String): RemoteConfigResponse {
        return api.getRemoteConfig(url)
    }

    suspend fun downloadFileIfNeeded(
        url: String?,
        fileName: String,
        folder: String,
        inCache: Boolean = false,
        retry: Boolean = false,
        downFailListener: ((reason: String) -> Unit)? = null
    ): File? {
        val file = FileUtils.createFile(fileName, folder, inCache)
        if (url.isNullOrEmpty()) {
            downFailListener?.invoke("url_empty")
            return null
        }
        if (FileUtils.isFileExist(file)) file.delete()
        Logger.logAction("Download url:${url}")
        val res = apiDownload.downloadFileAsync(url)
        if (res.body() != null && res.body()!!.contentLength() > 0 && res.isSuccessful) {
            res.body()?.byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
        } else {
            if (!retry) return downloadFileIfNeeded(url, fileName, folder, inCache, true)
        }
        return if (FileUtils.isFileExist(file)) file.let {
            Logger.logAction(" fileDownloaded:${file.absolutePath}")
            it
        } else {
            downFailListener?.invoke("down_fail")
            null
        }
    }

    suspend fun downloadAndUnZipFileIfNeeded(
        url: String?,
        fileName: String,
        folder: String,
        inCache: Boolean = false,
        retry: Boolean = false
    ): File? = runInIO {
        val file = FileUtils.createFile(fileName, folder, inCache)

        if (url.isNullOrEmpty()) return@runInIO null

        if (FileUtils.isFileExist(file)) file.delete()

        Logger.logAction("Download url: $url")

        try {
            val res = apiDownload.downloadFileAsync(url)
            if (res.body() != null && res.body()!!.contentLength() > 0 && res.isSuccessful) {
                res.body()?.byteStream().use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }
            } else {
                if (!retry) {
                    return@runInIO downloadAndUnZipFileIfNeeded(url, fileName, folder, inCache, true)
                } else {
                    Logger.logAction("Download failed: empty or error body")
                    return@runInIO null
                }
            }

            if (!FileUtils.isFileExist(file)) {
                Logger.logAction("File does not exist after download.")
                return@runInIO null
            }

            Logger.logAction("File downloaded: ${file.absolutePath}")

            if (file.name.endsWith(".zip", ignoreCase = true)) {
                val unzipDir = File(file.parentFile, file.nameWithoutExtension)

                if (unzipDir.exists()) {
                    unzipDir.deleteRecursively()
                }
                unzipDir.mkdirs()

                try {
                    ZipInputStream(BufferedInputStream(FileInputStream(file))).use { zipInput ->
                        var entry: ZipEntry?
                        while (zipInput.nextEntry.also { entry = it } != null) {
                            val outFile = File(unzipDir, entry!!.name)

                            if (entry!!.isDirectory) {
                                if (outFile.exists() && outFile.isFile) {
                                    outFile.delete()
                                }
                                outFile.mkdirs()
                            } else {
                                outFile.parentFile?.let { parent ->
                                    if (parent.exists() && !parent.isDirectory) {
                                        parent.delete()
                                    }
                                    parent.mkdirs()
                                }

                                FileOutputStream(outFile).use { output ->
                                    zipInput.copyTo(output)
                                }
                            }
                        }
                    }

                    Logger.logAction("Unzip success: ${unzipDir.absolutePath}")
                    return@runInIO unzipDir
                } catch (e: Exception) {
                    Logger.logAction("Unzip failed: ${e.message}")
                    return@runInIO file
                }
            } else return@runInIO file
        } catch (e: Exception) {
            Logger.logAction("Download or unzip failed: ${e.message}")
            return@runInIO null
        }
    }

    suspend fun saveImageToFile(imageId: String, faceId: String, bitmap: Bitmap): File? {
        return try {
            runInIO {
                val file = File(
                    appContext().filesDir,
                    "face_${imageId}_${faceId}_${System.currentTimeMillis()}.jpg"
                )
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun bitmapToUri(bitmap: Bitmap): Uri? {
        return try {
            runInIO {
                val file =
                    File(appContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                FileProvider.getUriForFile(
                    appContext(),
                    "${appContext().packageName}.provider",
                    file
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
