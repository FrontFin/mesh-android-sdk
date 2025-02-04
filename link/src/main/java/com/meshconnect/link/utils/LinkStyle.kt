package com.meshconnect.link.utils

import android.net.Uri
import android.util.Log
import org.json.JSONObject

internal data class LinkStyle(
    val th: String?,
)

internal fun getLinkStyleFromLinkUrl(url: String): LinkStyle? =
    try {
        val linkStyle = Uri.parse(url).getQueryParameter("link_style")
        linkStyle?.let {
            val obj = JSONObject(decodeBase64(linkStyle))
            LinkStyle(th = obj.optString("th"))
        }
    } catch (expected: Exception) {
        Log.e("SDK", "getLinkStyleFromLinkUrl", expected)
        null
    }
