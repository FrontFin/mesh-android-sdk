package com.meshconnect.link.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meshconnect.link.EventEmitter
import com.meshconnect.link.PayloadEmitter
import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.usecase.BroadcastLinkMessageUseCase
import com.meshconnect.link.usecase.DeserializeLinkMessageUseCase
import com.meshconnect.link.usecase.FilterLinkMessage
import com.meshconnect.link.utils.EventLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LinkViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val deserializeLinkMessageUseCase: DeserializeLinkMessageUseCase,
    private val payloadEmitter: PayloadEmitter,
    private val broadcastLinkMessageUseCase: BroadcastLinkMessageUseCase,
) : ViewModel() {

    internal val linkEvent = EventLiveData<LinkEvent>()
    internal val throwable = EventLiveData<Throwable>()
    private val _payloads = mutableSetOf<LinkPayload>()
    internal val payloads: List<LinkPayload> get() = _payloads.toList()
    internal var error: Throwable? = null; private set

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        error = exception
        throwable.emit(exception)
    }

    fun onJsonReceived(json: String) {
        viewModelScope.launch(exceptionHandler) {
            withContext(dispatcher) {
                runCatching { broadcastLinkMessageUseCase.launch(json) }
                runCatching { deserializeLinkMessageUseCase.launch(json) }
            }.onSuccess {
                if (it is LinkEvent.Payload) {
                    _payloads.add(it.payload)
                    error = null
                    payloadEmitter.emit(it.payload)
                }
                if (it != null) {
                    linkEvent.emit(it)
                }
            }.onFailure { throw it }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LinkViewModel(
                dispatcher = Dispatchers.IO,
                deserializeLinkMessageUseCase = DeserializeLinkMessageUseCase(JsonConverter),
                payloadEmitter = PayloadEmitter(),
                broadcastLinkMessageUseCase = BroadcastLinkMessageUseCase(
                    jsonConverter = JsonConverter,
                    filterLinkMessage = FilterLinkMessage,
                    eventEmitter = EventEmitter()
                )
            ) as T
        }
    }
}
