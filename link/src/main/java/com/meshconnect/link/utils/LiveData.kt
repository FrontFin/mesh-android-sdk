package com.meshconnect.link.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * This should be used for listening one short events.
 */
internal class EventLiveData<T> : LiveData<Event<T>>() {
    fun emit(value: T) = setValue(Event(value))
}

internal data class Event<out T>(val content: T) {
    private var consumed = false

    fun consume(block: (T) -> Unit) {
        if (consumed.not()) {
            consumed = true
            block(content)
        }
    }
}

internal fun <T> LifecycleOwner.observeEvent(
    liveData: EventLiveData<T>,
    body: (T) -> Unit,
) = liveData.observe(this) { it.consume(body) }

@Suppress("unused")
internal fun <T> Fragment.observeEvent(
    liveData: EventLiveData<T>,
    body: (T) -> Unit,
) = liveData.observe(viewLifecycleOwner) { it.consume(body) }
