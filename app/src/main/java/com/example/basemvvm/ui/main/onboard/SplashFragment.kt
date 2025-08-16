package com.example.basemvvm.ui.main.onboard

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.basemvvm.AppConfig
import com.example.basemvvm.Logger
import com.example.basemvvm.common.EventHelper
import com.example.basemvvm.common.utils.launchOnceWhenResumed
import com.example.basemvvm.common.utils.postDelayedSkipException
import com.example.basemvvm.config.ConfigManager
import com.example.basemvvm.databinding.FragmentSplashBinding
import com.example.basemvvm.ui.NavigationManager
import com.example.basemvvm.ui.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {

//    private val viewModelAd: MainAdViewModel by activityViewModels()
    private val viewModel: SplashViewModel by viewModels()
//    private val mainViewModel: MainViewModel by activityViewModels()
    private var activeLogcatCount = 0

    private var initJob: Job? = null
    private var waitForAdShowingJob: Job? = null
    private var isShowInterAd = false
    private var autoNavigateJob: Job? = null

    private var splashState = SplashState()

    private val backPressedCallback = object :
        OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {}
    }

    override fun makeBinding(inflater: LayoutInflater): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater)
    }

    override fun initViewAndData(saveInstanceState: Bundle?, binding: FragmentSplashBinding) {
//        binding.tvTitle.setTitleColor()
//        binding.tvSubTitle.setTitleColor()
        binding.root.setOnClickListener {
            if (++activeLogcatCount == 5) Logger.active()
        }

        initJob?.cancel()
        initJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
//            viewModelAd.initAdsDone.observe(viewLifecycleOwner) {
//                it?.run {
//                    startActionSplash()
//                    updateSplashState(initCMPDone = true)
//                }
//            }
            viewModel.getAppRemoteConfig()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000)
                updateSplashState(configDone = true)
            }
            delay(150)
//            activity?.let { viewModelAd.checkConsent(it) }
        }

        viewModel.configStatus.observe(viewLifecycleOwner) {
            if (it != null) updateSplashState(configDone = true)
        }

        /**tam thoi*/
        postDelayedSkipException(3000) {
            navigateAnim { NavigationManager.navigationToLanguage(parentFragmentManager) }
        }
    }

    private fun loadAdsSplash() {
//        viewBinding()?.let { binding ->
//            viewModelAd.loadBannerAd(
//                binding.bannerAd,
//                screenType = ScreenType.SPLASH.value,
//                caller = this@SplashFragment
//            )
//        }
//        viewModelAd.loadInterSplash()
    }

    override fun onDestroyView() {
        initJob?.cancel()
//        viewModelAd.releaseInterSplash()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
        autoNavigateJob?.cancel()
        autoNavigateJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            if (isShowInterAd) { runCatching { nextScreen() } }
        }
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
        autoNavigateJob?.cancel()
    }

    private fun startActionSplash() {
        waitForAdShowingJob?.cancel()
        waitForAdShowingJob = viewLifecycleOwner.lifecycleScope.launch {
//            if (viewModelAd.isInterSplashAvailableToShow()) {
//                delay(ConfigManager.timeWaitOpenSplash * 1000L)
//                viewModelAd.skip()
//            }
            EventHelper.unregister(this@SplashFragment)
            launchOnceWhenResumed { nextScreen() }
        }
    }

    private fun navigateAnim(onDone: () -> Unit) {
        onDone.invoke()
    }

    private fun showInterAdsSplash() {
//        if (viewModelAd.isAdAvailableInterAdSplash())
//            activity?.let {
//                isShowInterAd = true
//                viewBinding()?.loading?.invisible()
//                preLoadAdNextScreen(1000)
//                viewModelAd.showInterSplash(it, it.supportFragmentManager) { canShowAd ->
//                    launchOnceWhenResumed(500) { nextScreen() }
//                }
//            }
//        else nextScreen()
    }

    @Synchronized
    private fun nextScreen() {
        waitForAdShowingJob?.cancel()
        try {
            if (viewModel.isFirstOpenApp()) {
                preLoadAdNextScreen()
                if (AppConfig.isLanguageSetDone()) {
//                    navigateAnim { NavigationManager.navigateToIntro(parentFragmentManager) }
                } else {
                    navigateAnim { NavigationManager.navigationToLanguage(parentFragmentManager) }
                }
            } else {
//                navigateAnim { NavigationManager.navigateToHome(parentFragmentManager) }
            }
        } catch (e: Exception) {
            Logger.logAction("Error:${e.message}")
            e.printStackTrace()
        }
    }

    private fun preLoadAdNextScreen(delayTime: Long = 0) {
        if (viewModel.isFirstOpenApp()) {
//            viewModelAd.loadNativeAskAge1()
//            postDelayedSkipException(delayTime) { viewModelAd.loadNativeAskAge2() }
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: BaseInterAdManager.EventLoadDone) {
//        if (event.screenName != ScreenType.SPLASH.value) return
//        waitForAdShowingJob?.cancel()
//        launchOnceWhenResumed { showInterAdsSplash() }
//    }


    @Synchronized
    private fun updateSplashState(initCMPDone: Boolean? = null, configDone: Boolean? = null) {
        initCMPDone?.let { splashState.initCMPDone = it }
        configDone?.let { splashState.configDone = it }

        if (splashState.shouldLoadAds()) {
            splashState.adLoaded = true
            loadAdsSplash()
        }
    }

    class SplashState(
        var initCMPDone: Boolean = false,
        var configDone: Boolean = false,
        var adLoaded: Boolean = false
    ) {
        fun shouldLoadAds(): Boolean {
            return initCMPDone && configDone && !adLoaded
        }
    }
}