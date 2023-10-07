package com.meshconnect.link.store

import com.meshconnect.link.entity.LinkPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

val LinkPayloads: Flow<LinkPayload> get() = PayloadReceiver.payloads

internal object PayloadReceiver {

    internal val payloads get() = payloadFlow.asSharedFlow()

    private val payloadFlow = MutableSharedFlow<LinkPayload>()

    internal suspend fun emit(payload: LinkPayload) {
        payloadFlow.emit(payload)
    }
}
