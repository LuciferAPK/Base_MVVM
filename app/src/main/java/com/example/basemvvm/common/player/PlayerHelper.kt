package com.example.basemvvm.common.player

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.USAGE_NOTIFICATION_RINGTONE
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.example.basemvvm.R
import com.example.basemvvm.appContext
import com.example.basemvvm.common.display.DisplayManager
import com.example.basemvvm.common.utils.isOtherAppPlayingMedia
import java.io.File

object PlayerHelper {

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getMediaSource(context: Context, url: String?): MediaSource {
        if (url.isNullOrEmpty()) throw Exception("Path_Null")
        val uri = getUriFromPath(url)

        val dataSourceFactory: DataSource.Factory = when {
            url.startsWith("content://") -> {
                DefaultDataSource.Factory(context) {
                    ContentDataSource(context).apply { open(DataSpec(uri)) }
                }
            }

            url.startsWith("https") -> {
                getHttpDataSourceFactory(context)
            }

            url.startsWith("asset:///") -> {
                val dataSourceFactory = DataSource.Factory {
                    AssetDataSource(context)
                }
                dataSourceFactory
            }

            else -> {
                DefaultDataSource.Factory(context) { FileDataSource() }
            }
        }
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    @Synchronized
    private fun getUriFromPath(path: String): Uri {
        try {
            val file = File(path)
            return if (file.exists()) {
                Uri.fromFile(file)
            } else {
                Uri.parse(path)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Uri error: ${e.message}")
        }
    }

    @OptIn(UnstableApi::class)
    @Synchronized
    private fun getHttpDataSourceFactory(context: Context): DataSource.Factory {
        val httpDataSourceFactory =
            DefaultHttpDataSource.Factory().setUserAgent(getUserAgent(context))
                .setAllowCrossProtocolRedirects(true)

        val cache = PlayerCache.getInstance(appContext())
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        return cacheDataSourceFactory
    }

    @OptIn(UnstableApi::class)
    @Synchronized
    private fun getUserAgent(context: Context) = Util.getUserAgent(
        context, context.getString(R.string.app_name)
    )

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getExoPlayer(context: Context, mute: Boolean = true, loop: Boolean = true): ExoPlayer {
        val renderersFactory =
            DefaultRenderersFactory(context).setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true)

        val loadControl = getLoadControl()

        val trackSelector = DefaultTrackSelector(context, AdaptiveTrackSelection.Factory()).apply {
            val (maxWidth, maxHeight) = DisplayManager.getScreenWidthAndHeight(context)
            if (maxWidth > 0 && maxHeight > 0)
                setParameters(
                    buildUponParameters()
                        .setMaxVideoSize(maxWidth, maxHeight)
                        .setMaxVideoFrameRate(30)
                        .setForceLowestBitrate(true)
                        .setForceHighestSupportedBitrate(false)
                        .setPreferredTextLanguage(null)
                        .apply { if (mute) setRendererDisabled(C.TRACK_TYPE_AUDIO, true) }
                )
        }

        val exoPlayer = ExoPlayer.Builder(context).setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl).setUseLazyPreparation(true)
            .setTrackSelector(trackSelector).setSeekForwardIncrementMs(10000L)
            .setSeekBackIncrementMs(10000L).build()
        exoPlayer.apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 (API level 30)
                audioAttributes.setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            }
            setAudioAttributes(audioAttributes.build(), false)

            volume = if (mute) 0f else 1f
            repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        }
        return exoPlayer
    }

    @OptIn(UnstableApi::class)
    private fun getLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setBufferDurationsMs(
                1000, 2000, 500, 1000
            ).build()

    }

    private fun setPlayInNotificationSoundChannel(player: ExoPlayer) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
            .setUsage(USAGE_NOTIFICATION_RINGTONE)
        player.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 (API level 30)
                audioAttributes.setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            }
            setAudioAttributes(audioAttributes.build(), false)
        }
    }

    @OptIn(UnstableApi::class)
    fun playNotificationSound(url: String?, context: Context) {
        if (url.isNullOrEmpty()) return
        if (context.isOtherAppPlayingMedia()) return
        try {
            getExoPlayer(context, mute = false, loop = false).apply {
                setPlayInNotificationSoundChannel(this)
                setMediaSource(getMediaSource(context, url))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            release()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        release()
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(UnstableApi::class)
    fun playSound(url: String?, context: Context) {
        if (url.isNullOrEmpty()) return
        try {
            getExoPlayer(context, mute = false, loop = false).apply {
                setMediaSource(getMediaSource(context, url))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            release()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        release()
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playDefaultNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}