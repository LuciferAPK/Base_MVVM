package com.example.basemvvm.ads.cmp

import android.app.Activity
import android.content.Context
import com.example.basemvvm.Logger
import com.example.basemvvm.common.crashlytic.CrashlyticsHelper
import com.example.basemvvm.common.utils.runCatchException
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class GoogleMobileAdsConsentManager @Inject constructor(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = isUserFollowGDPR() != true || consentInformation.canRequestAds()

    private val isShownConsentForm = AtomicBoolean(false)
    private val isCallbackTrigger = AtomicBoolean(false)

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(
        activity: Activity,
        completeListener: OnConsentGatheringCompleteListener,
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        isCallbackTrigger.set(false)
        fun onDone(error: FormError? = null) {
            if (!isCallbackTrigger.getAndSet(true))
                completeListener.consentGatheringComplete(error)
        }

        if (isShownConsentForm.get() || isUserFollowGDPR() == false) {
            onDone()
            return
        }
        runCatchException({
            // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
//            val debugSettings =
//                ConsentDebugSettings.Builder(activity)
//                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                    // Check your logcat output for the hashed device ID e.g.
//                    // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
//                    // the debug functionality.
//                    .addTestDeviceHashedId("9D36630A119665D6E40FD6B305CC7821")
//                    .build()

            /*		val params =
                        ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()*/

            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                //.setConsentDebugSettings(debugSettings)
                .build()
            // Requesting an update to consent information should be called on every app launch.
            consentInformation.requestConsentInfoUpdate(
                activity, params, {
                    val requiredShowConsent =
                        consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED
                    val isFormAvailable = consentInformation.isConsentFormAvailable
                    if (requiredShowConsent && isFormAvailable) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError == null) isShownConsentForm.set(true)
                            onDone(formError)
                        }
                    } else {
                        isShownConsentForm.set(true)
                        onDone()
                    }
                },
                { requestConsentError ->
                    onDone(requestConsentError)
                }
            )
        }) { e ->
            CrashlyticsHelper.recordException(e)
            e.printStackTrace()
            Logger.logAction("Consent Error: ${e.message}")
            onDone()
        }
    }

    private fun isUserFollowGDPR(): Boolean? {
        val country = java.util.Locale.getDefault().country.uppercase()
        if (country.isEmpty()) return null
        val euCountries = listOf(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE",
            "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE",
            "IS", "LI", "NO"
        )

        return country in euCountries
    }

    /** Helper method to call the UMP SDK method to show the privacy options form. */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener,
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object {
        const val TAG = "ConsentManager"
    }
}