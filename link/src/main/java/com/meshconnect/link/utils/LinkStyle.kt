package com.meshconnect.link.utils

import android.util.Log
import org.json.JSONObject

internal data class LinkStyle(
    val th: String?
)

internal fun getLinkStyleFromLinkUrl(linkUrl: String): LinkStyle? {
    val linkStyle = getQueryParamFromUrl(linkUrl, "link_style")
    if (linkStyle != null) {
        try {
            val obj = JSONObject(decodeBase64(linkStyle))
            return LinkStyle(th = obj.optString("th"))
        } catch (expected: Exception) {
            Log.e("SDK", "getLinkStyleFromLinkUrl", expected)
        }
    }
    return null
}
