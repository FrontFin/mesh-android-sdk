package com.meshconnect.link.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meshconnect.link.broadcast.BroadcastLinkMessageImpl
import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.deserializer.DeserializeToMapImpl
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.store.PayloadReceiver
import com.meshconnect.link.usecase.BroadcastLinkMessageUseCase
import com.meshconnect.link.usecase.FilterLinkMessageImpl
import com.meshconnect.link.usecase.GetLinkEventUseCase
import com.meshconnect.link.utils.EventLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class LinkViewModel(
    private val getLinkEventUseCase: GetLinkEventUseCase,
    private val payloadReceiver: PayloadReceiver,
    private val broadcastLinkMessageUseCase: BroadcastLinkMessageUseCase
) : ViewModel() {

    internal val linkEvent = EventLiveData<LinkEvent>()
    internal val throwable = EventLiveData<Throwable>()
    private val _payloads = mutableSetOf<LinkPayload>()
    internal val payloads: List<LinkPayload> get() = _payloads.toList()
    internal var error: Throwable? = null; private set

    fun onJsonReceived(json: String) {
        viewModelScope.launch {
            getLinkEventUseCase.launch(json)
                .onSuccess {
                    if (it is LinkEvent.Payload) {
                        _payloads.add(it.payload)
                        error = null
                        payloadReceiver.emit(it.payload)
                    }
                    linkEvent.emit(it)
                }
                .onFailure {
                    error = it
                    throwable.emit(it)
                }
        }
        viewModelScope.launch {
            broadcastLinkMessageUseCase.launch(json)
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LinkViewModel(
                GetLinkEventUseCase(Dispatchers.IO, JsonConverter.get()),
                PayloadReceiver(Dispatchers.IO),
                BroadcastLinkMessageUseCase(
                    dispatcher = Dispatchers.IO,
                    deserializeToMap = DeserializeToMapImpl,
                    filterLinkMessage = FilterLinkMessageImpl,
                    broadcastLinkMessage = BroadcastLinkMessageImpl
                )
            ) as T
        }
    }
}
