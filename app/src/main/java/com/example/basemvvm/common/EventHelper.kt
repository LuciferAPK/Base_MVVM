package com.example.basemvvm.common

import org.greenrobot.eventbus.EventBus

object EventHelper {
    fun post(event: Any, isSticky: Boolean = false) {
        if (isSticky) EventBus.getDefault().postSticky(event)
        else
            EventBus.getDefault().post(event)
    }

    fun removeSticky(event: Any) {
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun register(subscriber: Any) {
        try {
            if (!EventBus.getDefault().isRegistered(subscriber))
                EventBus.getDefault().register(subscriber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister(subscriber: Any) {
        try {
            if (EventBus.getDefault().isRegistered(subscriber))
                EventBus.getDefault().unregister(subscriber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}