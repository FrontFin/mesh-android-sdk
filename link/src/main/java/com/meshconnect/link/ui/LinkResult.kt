package com.meshconnect.link.ui

import android.os.Parcelable
import com.meshconnect.link.entity.LinkPayload
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed interface LinkResult : Parcelable

@Parcelize
data class LinkSuccess(val payloads: List<LinkPayload>) : LinkResult

@Parcelize
data class LinkExit(val errorMessage: String? = null) : LinkResult {
    /**
     * Deprecated. Use [errorMessage] directly.
     *
     * Always `null` after the result is delivered via [LaunchLink] — the underlying
     * [Throwable] is not parcelable and cannot survive the Activity result round-trip.
     * Read [errorMessage] to obtain the error description.
     */
    @Deprecated(
        message =
            "Use errorMessage instead. " +
                "Throwable cannot survive the Parcelable round-trip; this property is always null " +
                "after activity-result delivery.",
        replaceWith = ReplaceWith("errorMessage"),
    )
    @IgnoredOnParcel
    val error: Throwable? = null

    /**
     * Deprecated constructor. Use `LinkExit(errorMessage = throwable?.message)` instead.
     */
    @Deprecated(
        message =
            "Use LinkExit(errorMessage = throwable?.message) instead. " +
                "Throwable cannot be safely parcelled; only the message string is preserved.",
        replaceWith = ReplaceWith("LinkExit(errorMessage = error?.message)"),
    )
    constructor(error: Throwable?) : this(errorMessage = error?.message)
}
