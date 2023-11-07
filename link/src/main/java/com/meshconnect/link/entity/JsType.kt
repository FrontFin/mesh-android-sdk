package com.meshconnect.link.entity

internal data class JsType(
    val type: Type?
)

@Suppress("EnumEntryName")
internal enum class Type {
    brokerageAccountAccessToken,
    error,
    close,
    done,
    showClose,
    transferFinished,
}

internal data class JsError(
    val errorMessage: String?
)
