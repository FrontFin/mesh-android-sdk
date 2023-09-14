package com.getfront.catalog.store

import com.getfront.catalog.entity.FrontPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

val FrontPayloads: Flow<FrontPayload> get() = FrontPayloadReceiver.payloads

internal object FrontPayloadReceiver {

    internal val payloads get() = payloadFlow.asSharedFlow()

    private val payloadFlow = MutableSharedFlow<FrontPayload>()

    internal suspend fun emit(payload: FrontPayload) {
        payloadFlow.emit(payload)
    }
}
