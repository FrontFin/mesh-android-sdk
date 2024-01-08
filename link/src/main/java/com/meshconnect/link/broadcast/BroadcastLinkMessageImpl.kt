package com.meshconnect.link.broadcast

import com.meshconnect.link.usecase.BroadcastLinkMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal object BroadcastLinkMessageImpl : BroadcastLinkMessage {
    private val messageFlow = MutableSharedFlow<Map<String, *>>()
    val sharedMessageFlow get() = messageFlow.asSharedFlow()

    override suspend fun invoke(map: Map<String, *>) {
        messageFlow.emit(map)
    }
}
