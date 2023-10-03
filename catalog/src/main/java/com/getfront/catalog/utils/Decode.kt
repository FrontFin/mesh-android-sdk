package com.getfront.catalog.utils

import java.util.Base64

internal fun decodeLink(source: String): String {
    return String(Base64.getDecoder().decode(source))
}
