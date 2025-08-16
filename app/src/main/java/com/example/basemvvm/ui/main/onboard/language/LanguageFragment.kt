package com.example.basemvvm.ui.main.onboard.language

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basemvvm.AppConfig
import com.example.basemvvm.common.display.DisplayManager
import com.example.basemvvm.common.utils.myEnableEdgeToEdge
import com.example.basemvvm.common.utils.setSafeOnClickScaleEffect
import com.example.basemvvm.databinding.FragmentLanguageBinding
import com.example.basemvvm.ui.NavigationManager
import com.example.basemvvm.ui.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding>() {

    private val viewModel: LanguageViewModel by viewModels()
//    private val viewModelAd: MainAdViewModel by activityViewModels()

    private var languageCodeSelected: String? = null
    private var isOneLanguageSelected: Boolean = false

    private val backPressedCallback = object :
        OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            activity?.finish()
        }
    }

    override fun makeBinding(inflater: LayoutInflater): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater)
    }

    override fun initViewAndData(saveInstanceState: Bundle?, binding: FragmentLanguageBinding) {
        binding.spaceStatusBar.updateLayoutParams {
            height = DisplayManager.getStatusBarHeight(requireContext())
        }
        val languageAdapter = LanguageAdapter { languageCode ->
            languageCodeSelected = languageCode
            if (!isOneLanguageSelected) {
                isOneLanguageSelected = true
                viewBinding()?.btnDone?.alpha = 1f
                showSecondNativeAd()
            }
        }

        languageAdapter.setData(viewModel.getListLanguagesByCurrentLocale(resources))

        binding.rcvLanguage.apply {
            if (AppConfig.reduceAnimation(context)) itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }

        binding.btnDone.setSafeOnClickScaleEffect {
            languageCodeSelected?.let {
                viewModel.saveLanguage(it)
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (!success || !isAdded || isStateSaved) return@observe
//            NavigationManager.navigateToIntro(parentFragmentManager)
        }

//        viewModelAd.loadingLFO1().observe(viewLifecycleOwner) {
//            it?.let { status ->
//                if (status == BaseNativeAdManager.LoadingAd.FAIL) binding.nativeLFO1.hide()
//                if (status == BaseNativeAdManager.LoadingAd.OFF) binding.nativeLFO1.off()
//            }
//        }
//
//        viewModelAd.nativeAdLFO1().observe(viewLifecycleOwner) { nativeAd ->
//            if (isDetached || isRemoving) return@observe
//            nativeAd?.let {
//                binding.nativeLFO1.visible()
//                binding.nativeLFO1.showAd(it)
//            }
//        }
//        viewModelAd.loadingLFO2().observe(viewLifecycleOwner) {
//            it?.let { status ->
//                if (status == BaseNativeAdManager.LoadingAd.FAIL) viewBinding()?.nativeLFO2?.hide()
//                if (status == BaseNativeAdManager.LoadingAd.OFF) viewBinding()?.nativeLFO2?.off()
//            }
//        }
//
//        viewModelAd.nativeAdLFO2().observe(viewLifecycleOwner) { nativeAd ->
//            if (isDetached || isRemoving) return@observe
//            nativeAd?.let {
//                binding.nativeLFO1.gone()
//                binding.nativeLFO2.visible()
//                binding.nativeLFO2.showAd(it)
//            }
//        }
//        viewModelAd.loadNativeLFO2()
    }

    override fun onStart() {
        super.onStart()
        activity?.myEnableEdgeToEdge(lightNavigationBar = true)
    }

    private fun showSecondNativeAd() {
//        viewModelAd.showNativeLFO2()
    }

    override fun onDestroyView() {
//        viewBinding()?.nativeLFO1?.release()
//        viewBinding()?.nativeLFO2?.release()
        super.onDestroyView()
//        viewModelAd.releaseNativeLFO()
    }

    override fun onResume() {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }
}