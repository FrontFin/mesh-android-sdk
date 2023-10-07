package com.meshconnect.link.ui

import android.os.Parcelable
import com.meshconnect.link.entity.LinkPayload
import kotlinx.parcelize.Parcelize

sealed interface LinkResult : Parcelable {
    @Parcelize
    data class Success(val payloads: List<LinkPayload>) : LinkResult
    @Parcelize
    data class Cancelled(val error: Throwable? = null) : LinkResult {
        val errorMessage: String? get() = error?.message
    }
}
