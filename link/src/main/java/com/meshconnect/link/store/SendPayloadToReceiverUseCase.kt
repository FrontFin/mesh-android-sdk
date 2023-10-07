package com.meshconnect.link.store

import com.meshconnect.link.entity.LinkPayload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SendPayloadToReceiverUseCase(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun launch(payload: LinkPayload) = withContext(dispatcher) {
        PayloadReceiver.emit(payload)
    }
}
