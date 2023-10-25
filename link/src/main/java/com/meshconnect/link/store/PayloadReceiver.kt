package com.meshconnect.link.store

import com.meshconnect.link.entity.LinkPayload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

private val payloadFlow = MutableSharedFlow<LinkPayload>()

val LinkPayloads: Flow<LinkPayload> get() = payloadFlow.asSharedFlow()

internal class PayloadReceiver(private val dispatcher: CoroutineDispatcher) {

    suspend fun emit(payload: LinkPayload) = withContext(dispatcher) {
        payloadFlow.emit(payload)
    }
}
