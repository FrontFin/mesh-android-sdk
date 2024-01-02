package com.meshconnect.link

import com.meshconnect.link.broadcast.BroadcastLinkMessageImpl

val LinkEvents get() = BroadcastLinkMessageImpl.sharedMessageFlow
