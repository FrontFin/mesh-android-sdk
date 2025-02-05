package com.meshconnect.link.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Suppress("unused")
internal fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(
    liveData: L,
    body: (T) -> Unit,
) = liveData.observe(this, NonNullObserver(body))

internal class NonNullObserver<T : Any>(val block: (T) -> Unit) : Observer<T> {
    override fun onChanged(data: T?) {
        requireNotNull(data)
        block(data)
    }
}
