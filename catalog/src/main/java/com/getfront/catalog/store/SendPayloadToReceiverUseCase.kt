package com.getfront.catalog.store

import com.getfront.catalog.entity.FrontPayload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SendPayloadToReceiverUseCase(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun launch(payload: FrontPayload) = withContext(dispatcher) {
        FrontPayloadReceiver.emit(payload)
    }
}
