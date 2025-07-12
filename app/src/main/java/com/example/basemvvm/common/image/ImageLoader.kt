package com.example.basemvvm.common.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.basemvvm.AppConfig
import com.example.basemvvm.R
import com.example.basemvvm.appContext
import com.example.basemvvm.common.display.DisplayManager
import com.example.basemvvm.common.utils.runCatchException
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

object ImageLoader {

    private val bgPlaceHolders = intArrayOf(
        R.drawable.ic_place_holder_one,
        R.drawable.ic_place_holder_two,
        R.drawable.ic_place_holder_three,
        R.drawable.ic_place_holder_four,
        R.drawable.ic_place_holder_five,
        R.drawable.ic_place_holder_six,
        R.drawable.ic_place_holder_seven,
        R.drawable.ic_place_holder_eight,
        R.drawable.ic_place_holder_nine,
        R.drawable.ic_place_holder_ten,
        R.drawable.ic_place_holder_eleven,
        R.drawable.ic_place_holder_twelve,
        R.drawable.ic_place_holder_thirteen,
        R.drawable.ic_place_holder_fourteen,
        R.drawable.ic_place_holder_fifteen,
        R.drawable.ic_place_holder_sixteen,
        R.drawable.ic_place_holder_seventeen,
        R.drawable.ic_place_holder_eighteen,
        R.drawable.ic_place_holder_nineteen,
        R.drawable.ic_place_holder_twenty
    )

    private val bgImgPlaceHolders = intArrayOf(
        R.drawable.img_place_holder_one,
        R.drawable.img_place_holder_two,
        R.drawable.img_place_holder_three,
        R.drawable.img_place_holder_four,
        R.drawable.img_place_holder_five,
        R.drawable.img_place_holder_six,
        R.drawable.img_place_holder_seven,
        R.drawable.img_place_holder_eight,
        R.drawable.img_place_holder_nine,
        R.drawable.img_place_holder_ten
    )

    private val counter = AtomicInteger(0)

    private fun getDiskCacheStrategy(): DiskCacheStrategy = DiskCacheStrategy.ALL
    private fun getDecodeFormat(): DecodeFormat =
        if (AppConfig.isSmallRamDevice(appContext()))
            DecodeFormat.PREFER_RGB_565
        else DecodeFormat.PREFER_ARGB_8888

    private val downloadRequests = CopyOnWriteArrayList<FutureTarget<File>>()

    @Synchronized
    fun cancelAllDownload() {
        try {
            for (futureTarget in downloadRequests) {
                Glide.with(appContext()).clear(futureTarget)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        downloadRequests.clear()
    }

    fun loadAssetImage(imageView: ImageView, fileName: String, loadAnimation: Boolean = true) {
        val indexPlaceHolder = counter.get() % bgPlaceHolders.size
        val placeHolder = bgPlaceHolders[indexPlaceHolder]
        val assetManager = imageView.context.assets
        val inputStream = assetManager?.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        try {
            var glideRequest = Glide.with(imageView.context)
                .load(bitmap)
                .placeholder(placeHolder)
                .format(getDecodeFormat())
                .priority(Priority.IMMEDIATE)
                .error(placeHolder)
                .diskCacheStrategy(getDiskCacheStrategy())

            if (loadAnimation && AppConfig.canRunAnim(appContext())) {
                glideRequest = glideRequest.transition(getLoadedTransition())
            }

            if (AppConfig.isLowPerformanceDevice(appContext())) {
                glideRequest = glideRequest.skipMemoryCache(true)
            }

            glideRequest.into(imageView)

            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearLoad(image: ImageView) {
        try {
            if (image.isAttachedToWindow)
                Glide.with(image.context).clear(image)
            else runCatching { Glide.with(appContext()).clear(image) }
        } catch (_: Exception) {
            runCatching { Glide.with(appContext()).clear(image) }
        }
    }

    fun loadFromResource(imageView: ImageView, resource: Int, loadAnimation: Boolean = true) {
        val context = imageView.context
        val indexPlaceHolder = counter.get() % bgPlaceHolders.size
        val placeHolder = bgPlaceHolders[indexPlaceHolder]
        counter.incrementAndGet()
        var glideRequest = Glide.with(context)
            .load(resource)
            .placeholder(placeHolder)
            .format(getDecodeFormat())
            .priority(Priority.IMMEDIATE)
            .error(placeHolder)
            .diskCacheStrategy(getDiskCacheStrategy())

        if (loadAnimation && AppConfig.canRunAnim(appContext())) {
            glideRequest = glideRequest.transition(getLoadedTransition())
        }

        if (AppConfig.isLowPerformanceDevice(appContext())) {
            glideRequest = glideRequest.skipMemoryCache(true)
        }

        glideRequest.into(imageView)
    }

    private fun getLoadedTransition() = DrawableTransitionOptions.withCrossFade(200)

    @SuppressLint("CheckResult")
    fun loadImage(
        imageView: ImageView,
        url: Any,
        needPlaceHolder: Boolean = true,
        loadAnimation: Boolean = true,
        needError: Boolean = true,
        noCache: Boolean = false,
        cornerRadius: Float = 0f,
        onDone: (success: Boolean) -> Unit = {},
    ) {
        try {

            val context = imageView.context
            val indexPlaceHolder = counter.get() % bgPlaceHolders.size
            val placeHolder = bgPlaceHolders[indexPlaceHolder]
            counter.incrementAndGet()
            var requestOptions = RequestOptions().format(getDecodeFormat())
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(getDiskCacheStrategy())
                .override(imageView.width, imageView.height)

            if (noCache) {
                requestOptions = requestOptions
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            }

            if (cornerRadius > 0) {
                val cornerPx = DisplayManager.dpToPx(imageView.context, dp = cornerRadius)
                requestOptions = requestOptions.transform(RoundedCorners(cornerPx))
            }

            if (needPlaceHolder) {
                requestOptions = requestOptions.placeholder(placeHolder)
            }

            if (needError) {
                requestOptions = requestOptions.error(placeHolder)
            }

            if (AppConfig.isLowPerformanceDevice(appContext())) {
                requestOptions = requestOptions.skipMemoryCache(true)
            }

            var glideRequest = Glide.with(context)
                .load(url)
                .apply(requestOptions)

            if (loadAnimation && AppConfig.canRunAnim(appContext())) {
                glideRequest = glideRequest.transition(getLoadedTransition())
            }
            glideRequest = glideRequest.listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    onDone.invoke(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    onDone.invoke(true)
                    return false
                }
            })
            glideRequest.into(imageView)
        } catch (e: Exception) {
            onDone.invoke(false)
            e.printStackTrace()
        }
    }

    fun download(url: String, options: RequestOptions? = null): File? {
        var requestOptions = options ?: RequestOptions()
            .override(Target.SIZE_ORIGINAL)
        requestOptions = requestOptions.priority(Priority.HIGH)
        try {
            return Glide.with(appContext())
                .downloadOnly()
                .load(url)
                .apply(requestOptions)
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun clearCache() {
        runCatchException({ Glide.get(appContext()).clearMemory() })
    }

    fun loadAdImage(image: ImageView, uri: Uri) {
        Glide.with(image.context)
            .load(uri)
            .into(image)
    }

    fun loadBitmapFromUrl(imageUrl: String, onBitmapLoaded: (Bitmap?) -> Unit) {
        Glide.with(appContext())
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onBitmapLoaded(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    onBitmapLoaded(null)
                }
            })
    }
}
