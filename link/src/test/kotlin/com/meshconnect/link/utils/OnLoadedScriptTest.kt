package com.meshconnect.link.utils

import com.meshconnect.link.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class OnLoadedScriptTest {
    @Test
    fun `test default constructor`() {
        val version = randomString
        val script = getOnLoadedScript(version)

        script shouldBeEqualTo
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version'"
    }

    @Test
    fun `test with accessTokens`() {
        val version = randomString
        val accessTokens = randomString

        val script = getOnLoadedScript(version, accessTokens)

        script shouldBeEqualTo
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version';window.accessTokens='$accessTokens'"
    }
}
