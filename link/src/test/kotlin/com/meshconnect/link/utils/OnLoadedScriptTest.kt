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
    fun `test with accessTokens & transferDestinationTokens`() {
        val version = randomString
        val accessTokens = randomString
        val transferDestinationTokens = randomString

        val script =
            getOnLoadedScript(
                version,
                accessTokens,
                transferDestinationTokens,
            )

        script shouldBeEqualTo
            StringBuilder()
                .append("window.meshSdkPlatform='android';window.meshSdkVersion='$version';")
                .append("window.accessTokens='$accessTokens';")
                .append("window.transferDestinationTokens='$transferDestinationTokens'")
                .toString()
    }
}
