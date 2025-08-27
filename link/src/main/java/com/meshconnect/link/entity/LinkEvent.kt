package com.meshconnect.link.entity

internal sealed interface LinkEvent {
    data object Done : LinkEvent

    data object Close : LinkEvent

    data object ShowClose : LinkEvent

    data object Loaded : LinkEvent

    data class TrueAuth(val link: String, val atomicToken: String) : LinkEvent

    data class Payload(val payload: LinkPayload) : LinkEvent
}
