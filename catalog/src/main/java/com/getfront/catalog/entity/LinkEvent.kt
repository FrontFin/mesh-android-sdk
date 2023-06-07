package com.getfront.catalog.entity

internal sealed interface LinkEvent {
    object Done : LinkEvent
    object Close : LinkEvent
    object ShowClose : LinkEvent
    object Undefined : LinkEvent
    data class Payload(val payload: FrontPayload) : LinkEvent
}
