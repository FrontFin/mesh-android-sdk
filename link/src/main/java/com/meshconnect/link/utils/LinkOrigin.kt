package com.meshconnect.link.utils

import java.net.URL

// `scheme://host[:port]`, the format WebView origin rules use.
internal fun URL.origin(): String {
    val origin = "${protocol.lowercase()}://${host.lowercase()}"
    return if (port == -1) origin else "$origin:$port"
}
