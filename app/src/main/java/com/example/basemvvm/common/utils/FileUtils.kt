package com.example.basemvvm.common.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.example.basemvvm.appContext
import com.example.basemvvm.common.image.ImageLoader
import com.example.basemvvm.common.utils.ContextExt.memoryByGB
import okhttp3.ResponseBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileUtils {
    private val BUFFER_SIZE: Int = if (memoryByGB > 2)
        DEFAULT_BUFFER_SIZE
    else 1024 * 4
    const val FOLDER_APP = "Wallpaper"
    const val PLAYLIST_FOLDER = "Playlist"
    const val firstFileName = "alo"
    private val TAG = "FileUtils"

    private val internalDir by lazy {
        val file = File(appContext().filesDir, FOLDER_APP)
        if (!file.exists()) {
            file.mkdir()
        }
        file
    }

    private val playlistFolder by lazy {
        val file = File(internalDir, PLAYLIST_FOLDER)
        if (!file.exists()) {
            file.mkdir()
        }
        file
    }

    val exoplayerCacheDir by lazy {
        createCacheFolder("exoplayer_video_cached")
    }

    fun getLength(file: File?): Long {
        if (file == null) return 0
        if (file.isDirectory) {
            return getDirLength(file)
        }
        return getFileLength(file)
    }

    private fun getDirLength(dir: File): Long {
        if (!isDir(dir)) return 0
        var len: Long = 0
        val files = dir.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                len += if (file.isDirectory) {
                    getDirLength(file)
                } else {
                    file.length()
                }
            }
        }
        return len
    }

    private fun getFileLength(file: File): Long {
        if (!isFile(file)) return -1
        return file.length()
    }

    fun isFile(file: File?): Boolean {
        return file != null && file.exists() && file.isFile
    }

    fun isDir(file: File?): Boolean {
        return file != null && file.exists() && file.isDirectory
    }

    private fun createCacheFolder(folderName: String): File {
        val folder = File(appContext().cacheDir, folderName)
        if (!folder.exists()) folder.mkdir()
        return folder
    }

    fun createTempFile(fileName: String): File {
        if (!internalDir.exists()) internalDir.mkdir()
        return File(internalDir, fileName)
    }

    fun createFile(fileName: String, folderName: String, inCache: Boolean): File {
        val folder = createFileFolder(folderName, inCache)
        return File(folder, fileName)
    }

    fun createFileFolder(folderName: String, inCache: Boolean): File {
        return File(
            if (inCache) appContext().cacheDir else appContext().filesDir, folderName
        ).apply {
            if (!exists()) mkdirs()
        }
    }

    fun deleteFolder(folderName: String, inCache: Boolean): Boolean {
        if (folderName.isEmpty()) return true
        val folder = createFileFolder(folderName, inCache)
        if (!folder.exists()) return true
        return deleteFolder(folder)
    }

    private fun deleteFolder(folder: File): Boolean {
        if (folder.isDirectory) {
            folder.listFiles()?.forEach { child ->
                deleteFolder(child)
            }
        }
        return folder.delete()
    }


    fun copyFileToPlaylistFolder(fileExport: File, url: String): File {
        val file = getPlayListFile(url)
        file.setReadable(true)
        file.setWritable(true)
        fileExport.copyTo(file, true, BUFFER_SIZE)
        return file
    }

    fun getFileNameFromURL(url: String): String {
        return firstFileName.plus("_") + url.substring(url.lastIndexOf("/") + 1)
    }

    fun getPlayListFile(url: String): File {
        return File(playlistFolder, getFileNameFromURL(url))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun copyFileData(destination: Uri, fileExport: File) {
        try {
            appContext().contentResolver.openFileDescriptor(destination, "w")
                ?.use { p ->
                    ParcelFileDescriptor.AutoCloseOutputStream(p).use { parcel ->
                        val fileReader = ByteArray(BUFFER_SIZE)
                        fileExport.inputStream().use { inputStream ->
                            var read: Int
                            while (inputStream.read(fileReader).also { read = it } != -1) {
                                parcel.write(fileReader, 0, read)
                            }
                        }
                    }
                }
            appContext().contentResolver.openFileDescriptor(destination, "rw")
                ?.use { p ->
                    ExifInterface(p.fileDescriptor)
                        .apply {
                            val dateTime =
                                SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(
                                    Date()
                                )
                            setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, dateTime)
                            setAttribute(ExifInterface.TAG_DATETIME, dateTime)
                            setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, dateTime)
                            saveAttributes()
                        }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFileUsingFileName(fileName: String): Boolean {
        return getUriFromFileName(fileName)?.let {
            appContext().contentResolver.delete(
                it,
                MediaStore.Files.FileColumns.DISPLAY_NAME + "=?",
                arrayOf(fileName)
            ) > 0
        } ?: false
    }

    @SuppressLint("Range")
    fun getUriFromFileName(fileName: String): Uri? {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        return appContext().contentResolver.query(
            uri,
            projection,
            MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?",
            arrayOf(fileName),
            null
        )?.use {
            it.moveToFirst()
            if (it.count > 0) {
                val fileId = it.getLong(it.getColumnIndex(projection[0]))
                Uri.parse("$uri/$fileId")
            } else null
        }
    }

    /**
     * Create a folder that doesn't exist
     */
    fun createDir(fileDirName: String?, type: String? = null): File? {
        val fileDirPath = appContext().getExternalFilesDir(type)?.absolutePath
            ?: appContext().cacheDir.absolutePath
        var dir = "$fileDirPath/"
        val folder = fileDirName ?: ""
        if (folder.isNotBlank()) {
            dir += "$folder/"
        }
        return createDir(File(dir))
    }

    /**
     * Create a folder that doesn't exist
     */
    fun createDir(file: File?): File? {
        return if (createOrExistsDir(file)) file else null
    }

    fun createOrExistsDir(file: File?): Boolean {
        return file != null && (if (file.exists()) file.isDirectory else file.mkdirs())
    }

    /**
     * Return to cache folder
     * After the app is uninstalled, the data in the App-specific directory will be deleted.
     * Under the premise of Android Q, if you declare in AndroidManifest.xml: android:hasFragileUserData="true",
     * the user can choose whether to keep it.
     */
    fun getCacheFile(): File {
        var cacheFile = appContext().externalCacheDir
        cacheFile = cacheFile ?: createDir("glide")
        return cacheFile ?: appContext().cacheDir
    }

    /**
     * Get Glide cache directory file
     */
    fun getGlideCacheFile(): File? {
        return createDir(File(getCacheFile(), "glide"))
    }

    /**
     * Get Glide cache size
     */
    fun getGlideCacheSize(): Long {
        return getLength(getGlideCacheFile())
    }

    fun getFileName(url: String, index: Int = 1): String {
        val subString = url.split("/")
        if (subString.isNotEmpty()) return subString[subString.size - index]
        return url
    }

    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun getAudioUriForAndroidHigherQ(downloadFile: File): Uri? {
        val context = appContext()
        var uri: Uri? = null
        val audioCollectionUri =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues()
        values.put(MediaStore.Audio.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, downloadFile.name)
        values.put(MediaStore.Audio.Media.DATA, downloadFile.absolutePath)
        values.put(MediaStore.Audio.Media.IS_PENDING, 1)
        val cursor = context.contentResolver.query(
            audioCollectionUri,
            null,
            MediaStore.MediaColumns.DATA + "=?",
            arrayOf(downloadFile.absolutePath),
            null
        )
        uri = if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
            // Ringtone ready existed on Database > Update
            val id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            ContentUris.withAppendedId(audioCollectionUri, java.lang.Long.valueOf(id))
        } else {
            try {
                context.contentResolver.insert(audioCollectionUri, values)
            } catch (ex: Exception) {
                if (uri != null) {
                    context.contentResolver.delete(uri, null, null)
                }
                values.clear()
                return null
            }

        }
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        if (uri != null) {
            context.contentResolver.update(uri, values, null, null)
        }
        return uri
    }

    fun getDownloadedFile(url: String): File {
        val pathFile = getFileName(url)
        val fullPathFile = getFilePathFromFileName(appContext(), pathFile)
        return File(fullPathFile)
    }

    private fun getFilePathFromFileName(context: Context, fileName: String): String {
        val externalDir: String? =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
        return "$externalDir/$fileName"
    }

    fun write(body: ResponseBody, fileName: String): File? {
        var file: File? = createTempFile(fileName)
        try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                delete(file)
                val fileReader = ByteArray(BUFFER_SIZE)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)
                var read: Int
                while (inputStream.read(fileReader).also { read = it } != -1) {
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                delete(file)
                file = null
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            delete(file)
            file = null
        } catch (e: Exception) {
            e.printStackTrace()
            delete(file)
            file = null
        }
        return file
    }

    fun delete(file: File?) {
        try {
            if (file?.exists() == true)
                file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addImageToGallery(fileExport: File, fileName: String): File {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Files.FileColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "image/jpeg")
        }
        val contentResolver = appContext().contentResolver
        fileExport.setLastModified(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            var file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            file.setReadable(true)
            file.setWritable(true)
            file = fileExport.copyTo(file, true, BUFFER_SIZE)
            values.put(MediaStore.Files.FileColumns.DATA, file.path)
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            file.setLastModified(System.currentTimeMillis())
            return file
        } else {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
            val uri = contentResolver.insert(
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                values
            )?.also { copyFileData(it, fileExport) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            uri?.also { contentResolver.update(it, values, null, null) }
        }
        return fileExport
    }

    private fun copyFile(sourceFile: File, outputStream: OutputStream) {
        outputStream.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }
    }

    fun File.lastName(): String {
        return try {
            path.substring(path.lastIndexOf("/") + 1)
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
    }

    fun copyFileFromUri(uriString: String): File? {
        try {
            val uri = Uri.parse(uriString)
            val fileName = getFileNameFromUri(uri) ?: (System.currentTimeMillis().toString() + "." +
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))
            val file = File(videoFolder, System.currentTimeMillis().toString() + "_" + fileName)
            if (file.exists()) file.delete()
            val outputStream = FileOutputStream(file)
            appContext().contentResolver.openInputStream(uri)?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private val videoFolder by lazy {
        val file = File(internalDir, "Video")
        if (!file.exists()) {
            file.mkdir()
        }
        file
    }

    fun getFileNameFromUri(uri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            if (uri == null) null
            else appContext().run { contentResolver.query(uri, null, null, null, null) }?.run {
                cursor = this
                val nameIndex = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                moveToFirst()
                getString(nameIndex)
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    fun copyWallpaperVideo(path: String?): File? {
        if (path == null) return null
        val fileExport = File(path)
        val file =
            File(videoFolder, System.currentTimeMillis().toString() + "_" + fileExport.lastName())
        try {
            if (file.exists()) file.delete()
            fileExport.copyTo(file, true, BUFFER_SIZE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun downloadAndSaveToFilesDir(url: String, fileName: String, folder: String): File? {
        try {
            val downloadedFile: File = ImageLoader.download(url) ?: return null
            if (downloadedFile.exists()) {
                val destDir = File(appContext().filesDir, folder)
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                val destinationFile = File(destDir, fileName)
                Files.copy(
                    downloadedFile.toPath(),
                    destinationFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                downloadedFile.delete()
                return destinationFile
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun saveBitmapToFile(
        bitmap: Bitmap,
        fileName: String,
        folderName: String,
        inCache: Boolean
    ): File? {
        val file = createFile(fileName, folderName, inCache)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            return file
        } catch (e: IOException) {
            bitmap.recycle()
            e.printStackTrace()
            return null
        }
    }

    fun unzip(zipFilePath: String, targetDirPath: String) {
        val buffer = ByteArray(1024)
        val targetDir = File(targetDirPath)
        if (!targetDir.exists()) targetDir.mkdirs()
        val canonicalTargetDir = targetDir.canonicalPath

        ZipInputStream(FileInputStream(zipFilePath)).use { zis ->
            var zipEntry: ZipEntry? = zis.nextEntry
            while (zipEntry != null) {
                val newFile = File(targetDir, zipEntry.name).canonicalFile

                // Chống path traversal
                if (!newFile.path.startsWith(canonicalTargetDir)) {
                    throw IOException("Invalid entry: ${zipEntry.name}")
                }

                if (zipEntry.isDirectory || zipEntry.name.endsWith("/")) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
        }
    }

    fun isFileExist(file: File): Boolean {
        return try {
            file.let { it.exists() && it.length() > 0 }
        } catch (e: Exception) {
            false
        }
    }

    fun isFileExist(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            File(path).let { it.exists() && it.length() > 0 }
        } catch (e: Exception) {
            false
        }
    }

    fun getFileExtension(url: String): String {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)
        return if (!fileExtension.isNullOrEmpty()) {
            ".$fileExtension"
        } else {
            ""
        }
    }

    fun copyAssetToFileDir(assetFileName: String?, outputDir: String): String? {
        if (assetFileName.isNullOrEmpty()) return null
        val outputFile = File(appContext().cacheDir, "$outputDir/$assetFileName")
        if (outputFile.parentFile?.exists() != true) {
            outputFile.parentFile?.mkdirs() // Tạo thư mục nếu chưa tồn tại
        }

        try {
            appContext().assets.open(assetFileName).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return outputFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun compressImage(imageFile: File, maxSize: Int = 1080, quality: Int = 80): File? {
        try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            // Tính toán kích thước mới (giữ nguyên tỷ lệ)
            val (newWidth, newHeight) = calculateNewSize(bitmap.width, bitmap.height, maxSize)

            // Resize ảnh
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Lưu ảnh đã nén vào file mới
            val compressedFile = File(imageFile.parent, "compressed_${imageFile.name}")
            FileOutputStream(compressedFile).use { outputStream ->
                resizedBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    quality,
                    outputStream
                ) // Tỷ lệ nén do người dùng chọn
            }

            return compressedFile // Trả về file ảnh đã nén
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun calculateNewSize(width: Int, height: Int, maxSize: Int): Pair<Int, Int> {
        return if (width > height) {
            val newHeight = (height.toFloat() / width * maxSize).toInt()
            maxSize to newHeight
        } else {
            val newWidth = (width.toFloat() / height * maxSize).toInt()
            newWidth to maxSize
        }
    }

    fun deleteFile(filePath: String) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                File(filePath).apply {
                    if (exists()) delete()
                }
            } else {
                File(filePath).apply {
                    val resolver = appContext().contentResolver
                    val existingUri = findFileInDownload(appContext(), name)
                    existingUri?.let { resolver.delete(it, null, null) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun renameFile(filePath: String, newName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val file = File(filePath)

            if (!file.exists()) {
                return false
            }

            val parentDir = file.parentFile ?: return false

            val newFile = File(parentDir, newName)

            if (newFile.exists()) {
                return false
            }

            return file.renameTo(newFile)
        } else {
            val contentResolver = appContext().contentResolver
            val file = File(filePath)

            // Kiểm tra file có tồn tại không
            if (!file.exists()) {
                return false
            }

            // Lấy URI của file từ MediaStore
            val uri: Uri = getFileUri(appContext(), filePath) ?: return false

            // Tạo ContentValues để cập nhật tên file
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newName) // Đặt tên mới
            }

            // Cập nhật tên file trong MediaStore
            val updatedRows = contentResolver.update(uri, contentValues, null, null)

            return updatedRows > 0
        }
    }

    private fun getFileUri(context: Context, filePath: String): Uri? {
        val contentResolver = context.contentResolver
        val file = File(filePath)

        // Chọn MediaStore phù hợp với loại file
        val externalUri = MediaStore.Files.getContentUri("external")

        // Query để lấy URI từ file path
        val cursor: Cursor? = contentResolver.query(
            externalUri,
            arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DATA + "=?",
            arrayOf(file.absolutePath),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(externalUri, id.toString())
            }
        }
        return null
    }


    fun existInDownloadFolder(fileName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val file = getStorageFile(fileName)
            return file.exists() && file.length() > 0
        } else {
            return findFileInDownload(appContext(), fileName) != null
        }
    }

    fun getDefaultFilePath(fileName: String?): String {
        return FileUtils.createFile(
            fileName ?: "download_file_${System.currentTimeMillis()}.raw", "download", false
        ).absolutePath
    }

    fun copyFileToDownloadFolder(sourceFile: File): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val fileInDownload = getStorageFile(sourceFile.name)
            if (copyFileWithFileAPI(
                    sourceFile,
                    fileInDownload
                )
            ) return fileInDownload.absolutePath
        } else {
            return copyFileToDownloadWithMediaStore(appContext(), sourceFile)
        }
        return null
    }

    private fun getDirectory() = Environment.DIRECTORY_DOWNLOADS

    private fun getFolder() = "Face_Swap"

    private fun getStorageFile(fileName: String): File {
        val downloadFolder =
            Environment.getExternalStoragePublicDirectory(getDirectory())

        if (!downloadFolder.exists() && !downloadFolder.mkdirs()) {
            throw IOException("Failed to create directory: ${downloadFolder.absolutePath}")
        }
        val folder = File(downloadFolder, getFolder())

        if (!folder.exists() && !folder.mkdirs()) {
            throw IOException("Failed to create folder: ${folder.absolutePath}")
        }

        val file = File(folder, fileName)
        if (!file.exists() && !file.createNewFile()) {
            throw IOException("Failed to create file: ${file.absolutePath}")
        }

        return file
    }

    private fun copyFileWithFileAPI(sourceFile: File, destFile: File): Boolean {
        return try {
            sourceFile.inputStream().use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true // Copy thành công
        } catch (e: Exception) {
            e.printStackTrace()
            false // Copy thất bại
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyFileToDownloadWithMediaStore(context: Context, sourceFile: File): String? {
        val resolver = context.contentResolver
        val relativePath = "${getDirectory()}/${getFolder()}"

        val existingUri = findFileInDownload(context, sourceFile.name)
        existingUri?.let { resolver.delete(it, null, null) }

        val existingUri2 = findFileInDownload(context, "_${sourceFile.name}")
        existingUri2?.let { resolver.delete(it, null, null) }

        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
            put(MediaStore.Downloads.IS_TRASHED, 0)
        }

        val newUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        newUri?.let { targetUri ->
            resolver.openOutputStream(targetUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(targetUri, contentValues, null, null)
            MediaScannerConnection.scanFile(
                context,
                arrayOf(Environment.getExternalStoragePublicDirectory(getDirectory()).absolutePath),
                arrayOf("application/octet-stream"),
                null
            )
            return getFileFullPathFromUri(context, targetUri, relativePath)
        }

        return null
    }

    private fun getFileFullPathFromUri(
        context: Context,
        uri: Uri,
        relativePath: String,
    ): String? {
        val resolver = context.contentResolver
        resolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val fileName = cursor.getString(0) // Lấy tên file mới
                    val fullPath =
                        Environment.getExternalStoragePublicDirectory(relativePath).absolutePath + "/" + fileName
                    return fullPath
                }
            }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findFileInDownload(context: Context, fileName: String): Uri? {
        val resolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        resolver.query(
            uri,
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            ),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                return ContentUris.withAppendedId(uri, id)
            }
        }
        return null
    }
}