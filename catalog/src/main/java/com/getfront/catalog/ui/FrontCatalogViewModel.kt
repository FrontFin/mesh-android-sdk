package com.getfront.catalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.entity.FrontPayload
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.store.SendPayloadToReceiverUseCase
import com.getfront.catalog.usecase.GetLinkEventUseCase
import com.getfront.catalog.utils.EventLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class FrontCatalogViewModel(
    private val getLinkEventUseCase: GetLinkEventUseCase,
    private val sendPayloadToReceiverUseCase: SendPayloadToReceiverUseCase
) : ViewModel(), JSBridge.Callback {

    internal val linkEvent = EventLiveData<LinkEvent>()
    internal val throwable = EventLiveData<Throwable>()
    private val _payloads = mutableSetOf<FrontPayload>()
    internal val payloads: List<FrontPayload> get() = _payloads.toList()
    internal var error: Throwable? = null; private set

    override fun onJsonReceived(payloadJson: String) {
        viewModelScope.launch {
            getLinkEventUseCase.launch(payloadJson)
                .onSuccess {
                    if (it is LinkEvent.Payload) {
                        _payloads.add(it.payload)
                        error = null
                        sendPayloadToReceiverUseCase.launch(it.payload)
                    }
                    linkEvent.emit(it)
                }
                .onFailure {
                    error = it
                    throwable.emit(it)
                }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FrontCatalogViewModel(
                GetLinkEventUseCase(Dispatchers.IO, JsonConverter.get()),
                SendPayloadToReceiverUseCase(Dispatchers.IO)
            ) as T
        }
    }
}
