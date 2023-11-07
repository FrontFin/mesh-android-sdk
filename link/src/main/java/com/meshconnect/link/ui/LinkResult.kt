package com.meshconnect.link.ui

import android.os.Parcelable
import com.meshconnect.link.entity.LinkPayload
import kotlinx.parcelize.Parcelize

sealed interface LinkResult : Parcelable

@Parcelize
data class LinkSuccess(val payloads: List<LinkPayload>) : LinkResult

@Parcelize
data class LinkExit(val error: Throwable? = null) : LinkResult {
    val errorMessage: String? get() = error?.message
}
