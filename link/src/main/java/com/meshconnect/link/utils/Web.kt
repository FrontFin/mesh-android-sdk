package com.meshconnect.link.utils

import com.meshconnect.link.BuildConfig

@Suppress("MaxLineLength")
internal val meshSDKPlatformScript: String
    get() {
        val version = BuildConfig.VERSION
        return "window.meshSdkPlatform='android';window.meshSdkVersion='$version';if(window.parent) {window.parent.meshSdkPlatform='android';window.parent.meshSdkVersion='$version';}"
    }
