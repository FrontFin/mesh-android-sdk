package com.getfront.catalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.getfront.catalog.di.di
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.usecase.GetLinkEventUseCase
import com.getfront.catalog.utils.EventLiveData
import kotlinx.coroutines.launch

internal class FrontCatalogViewModel(
    private val getLinkEventUseCase: GetLinkEventUseCase
) : ViewModel(), JSBridge.Callback {

    internal val linkEvent = EventLiveData<LinkEvent>()
    internal val throwable = EventLiveData<Throwable>()

    override fun onJsonReceived(payloadJson: String) {
        viewModelScope.launch {
            getLinkEventUseCase.launch(payloadJson)
                .onSuccess { linkEvent.emit(it) }
                .onFailure { throwable.emit(it) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FrontCatalogViewModel(di.getLinkEventUseCase) as T
        }
    }
}
