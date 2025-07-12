package com.example.basemvvm.common.crashlytic

import com.example.basemvvm.common.utils.backgroundLaunchSafe
import com.example.basemvvm.common.utils.isNullOrEmptyOrBlank
import com.example.basemvvm.common.utils.runCatchException

object CrashlyticsHelper {
    fun recordException(e: Exception, logMessage: String? = null) {
        backgroundLaunchSafe {
//            runCatchException({ FirebaseCrashlytics.getInstance().recordException(e) })
//            if (!logMessage.isNullOrEmptyOrBlank())
//                logEvent("Exception", logMessage!!)
        }
    }

    fun recordException(e: Error, logMessage: String? = null) {
//        runCatchException({ FirebaseCrashlytics.getInstance().recordException(e) })
//        if (!logMessage.isNullOrEmptyOrBlank())
//            logEvent("Exception", logMessage!!)
    }

    fun logEvent(eventName: String, logMessage: String) {
        runCatchException({
//            FirebaseCrashlytics.getInstance().setCustomKey("event_name", eventName)
//            FirebaseCrashlytics.getInstance().log(logMessage)
        })
    }

    fun logOpenScreen(screen: Any) {
        backgroundLaunchSafe {
            logEvent("open_screen", if (screen is String) screen else screen::class.java.simpleName)
        }
    }
}