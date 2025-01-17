package com.meshconnect.link

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private val events = MutableSharedFlow<Map<String, *>>()

val LinkEvents get() = events.asSharedFlow()

internal class EventEmitter {
    suspend fun emit(event: Map<String, *>) {
        events.emit(event)
    }
}
