package com.example.basemvvm

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.example.basemvvm.ads.cmp.GoogleMobileAdsConsentManager
import com.example.basemvvm.common.display.DisplayManager
import com.example.basemvvm.common.utils.backgroundLaunch
import com.example.basemvvm.data.repository.AppRepository
import dagger.hilt.android.HiltAndroidApp
import dagger.Lazy
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class MyApp : MultiDexApplication(), DefaultLifecycleObserver {
    private var activityCount = 0
    var activeLog = false

    fun getActivityCount() = activityCount

    @Inject
    lateinit var appRepository: Lazy<AppRepository>

    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>

    @Inject
    lateinit var mGoogleMobileAdsConsentManager: Lazy<GoogleMobileAdsConsentManager>

    var isEnterHome = false

//    var fontPreloader = FontPreloader(this)

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .build()
//            )
//        }
//        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
//            handleUncaughtException(thread, exception)
//        }
//        Looper.getMainLooper().thread.setUncaughtExceptionHandler { thread, throwable ->
//            handleUncaughtException(thread, throwable)
//        }

        time_open_app = System.currentTimeMillis()
        instance = this
        Logger.init()
        DisplayManager.init(applicationContext)
        AppConfig.detectAppLanguage()
//        AdjustManager.initAdjust()
//        registerActivityLifecycleCallbacks(AdjustManager.AdjustLifecycleCallbacks())
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        listenActivities()
        backgroundLaunch {
            preloadFonts()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
//        AppConfig.isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
//        AppConfig.isAppInForeground = false
    }

    private fun handleUncaughtException(thread: Thread, exception: Throwable) {
        Logger.d("AppUncaughtException", exception.message.toString())

//        CrashlyticsHelper.recordException(Exception("AppUncaughtException", exception))

//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(intent)
        Runtime.getRuntime().exit(1)
    }

    private fun listenActivities() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
                activityCount--
//                if (activityCount == 0)
//                    postDelayedSkipException(500) {
//                        if (activityCount == 0) {
//                            isShowNotPermissionDialog = true
//                            onAllActivitiesDestroyed()
//                        }
//                    }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    private fun onAllActivitiesDestroyed() {
//        postDelayedSkipException {
//            rewardedAd.get().release()
//            interHome.get().release()
//            openAd.get().release()
//            nativeHomeList.get().release()
//            nativeFinish.get().release()
//            nativeResult.get().release()
//            nativeDetailList.get().release()
//            nativeAdLoadingSwapVideo.get().release()
//            eventTrackingManager.get().resetEventOrder()
//        }
    }

//    fun isAdMobAllowed() = mGoogleMobileAdsConsentManager.get().canRequestAds
//
//    fun isAdDisabled() = ConfigManager.offAdVersionCode == BuildConfig.VERSION_CODE

//    fun canLoadAd() = isAdMobAllowed() && !isVip() && !isAdDisabled()

    companion object {
        var time_open_app: Long = 0
        lateinit var instance: MyApp
        fun getContext(): Context = instance.applicationContext
    }

    private fun preloadFonts() {
//        fontPreloader.run {
//            preloadFont(R.font.my_font_regular)
//            preloadFont(R.font.my_font_medium)
//            preloadFont(R.font.my_font_semibold)
//            preloadFont(R.font.my_font_bold)
//            preloadFont(R.font.my_font_extrabold)
//        }
    }

    fun isVip() = false
}

fun myApp() = MyApp.instance
fun appContext() = MyApp.getContext()
fun appResources(): Resources = AppConfig.getLocalizedContext(MyApp.getContext()).resources
fun appRepository(): AppRepository = MyApp.instance.appRepository.get()
