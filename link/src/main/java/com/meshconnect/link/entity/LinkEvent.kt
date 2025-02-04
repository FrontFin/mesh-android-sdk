package com.meshconnect.link.entity

internal sealed interface LinkEvent {
    object Done : LinkEvent
    object Close : LinkEvent
    object ShowClose : LinkEvent
    object Loaded : LinkEvent
    data class Payload(val payload: LinkPayload) : LinkEvent
}
