package com.meshconnect.link

import com.meshconnect.link.broadcast.BroadcastLinkMessageImpl

val LinkEvent get() = BroadcastLinkMessageImpl.sharedMessageFlow
