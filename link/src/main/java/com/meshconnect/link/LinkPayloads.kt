package com.meshconnect.link

import com.meshconnect.link.entity.LinkPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private val payloads = MutableSharedFlow<LinkPayload>()

val LinkPayloads get() = payloads.asSharedFlow()

internal class PayloadEmitter {
    suspend fun emit(payload: LinkPayload) {
        payloads.emit(payload)
    }
}
