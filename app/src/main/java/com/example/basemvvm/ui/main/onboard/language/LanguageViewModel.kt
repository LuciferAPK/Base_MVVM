package com.example.basemvvm.ui.main.onboard.language

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basemvvm.AppConfig
import com.example.basemvvm.R
import com.example.basemvvm.common.utils.runInIO
import com.example.basemvvm.data.local.PreferencesKey
import com.example.basemvvm.data.local.PreferencesManager
import com.example.basemvvm.data.model.LanguageModel
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val preferencesManager: Lazy<PreferencesManager>
) : ViewModel() {

    private val _saveStatus = MutableLiveData(false)
    val saveStatus: LiveData<Boolean> get() = _saveStatus

    fun getListLanguagesByCurrentLocale(resources: Resources): List<LanguageModel> {
        val currentLanguage = Locale.getDefault().language
        val languageList = getListLanguage(resources)
        val selectedLanguage = languageList.find { it.languageCode == currentLanguage }
        return if (selectedLanguage != null) {
            listOf(selectedLanguage) + languageList.filterNot { it.languageCode == currentLanguage }
        } else {
            languageList
        }
    }

    private fun getListLanguage(resources: Resources): List<LanguageModel> = listOf(
        LanguageModel(R.drawable.ic_english, resources.getString(R.string.english), "en"),
        LanguageModel(R.drawable.ic_hindi, resources.getString(R.string.hindi), "hi"),
        LanguageModel(R.drawable.ic_france, resources.getString(R.string.french), "fr"),
        LanguageModel(R.drawable.ic_spain, resources.getString(R.string.spanish), "es"),
        LanguageModel(R.drawable.ic_portugal, resources.getString(R.string.portuguese), "pt"),
        LanguageModel(R.drawable.ic_indonesia, resources.getString(R.string.indonesia), "in"),
        LanguageModel(R.drawable.ic_korea, resources.getString(R.string.korean), "ko"),
        LanguageModel(R.drawable.ic_japan, resources.getString(R.string.japanese), "ja"),
        LanguageModel(R.drawable.ic_germany, resources.getString(R.string.german), "de"),
        LanguageModel(R.drawable.ic_vietnam, resources.getString(R.string.vietnamese), "vi"),
        LanguageModel(R.drawable.ic_turkey, resources.getString(R.string.turkish), "tr")
    )

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            runInIO {
                preferencesManager.get().save(PreferencesKey.KEY_LANGUAGE, languageCode)
                AppConfig.detectAppLanguage()
            }
            _saveStatus.value = true
        }
    }
}
