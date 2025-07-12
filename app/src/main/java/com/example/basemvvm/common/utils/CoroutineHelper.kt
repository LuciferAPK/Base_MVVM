package com.example.basemvvm.common.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun getMainScope(): CoroutineScope {
    return CoroutineScope(Dispatchers.Main)
}

fun mainLaunch(block: suspend CoroutineScope.() -> Unit) = getMainScope().launch { block() }

fun mainLaunchSafe(block: suspend CoroutineScope.() -> Unit) = getMainScope().launch {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun backgroundLaunch(block: suspend CoroutineScope.() -> Unit) =
    getMainScope().launch(Dispatchers.Default) { block() }

fun backgroundLaunchSafe(block: suspend CoroutineScope.() -> Unit) =
    getMainScope().launch(Dispatchers.Default) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

fun AndroidViewModel.launch(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch { block() }

fun ViewModel.launch(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch { block() }

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit) =
    lifecycleScope.launch { block() }

suspend fun <T> runInBackground(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Default) { block() }

suspend fun <T> runInIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO) { block() }

suspend fun <T> runInMain(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main) { block() }

fun LifecycleOwner.launchOnceWhenResumed(block: suspend CoroutineScope.() -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        lifecycleScope.launch { block() }
    } else
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
                this@launch.cancel()
            }
        }
}

fun LifecycleOwner.launchOnceWhenStarted(block: suspend CoroutineScope.() -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        lifecycleScope.launch { block() }
    } else
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
                this@launch.cancel()
            }
        }
}