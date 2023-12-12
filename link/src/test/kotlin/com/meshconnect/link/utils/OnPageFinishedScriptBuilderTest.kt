package com.meshconnect.link.utils

import com.meshconnect.link.randomString
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

@Suppress("MaxLineLength")
class OnPageFinishedScriptBuilderTest {
    @Test
    fun `test default constructor`() {
        val version = randomString
        val script = OnPageFinishedScriptBuilder(version).build()
        assertEquals(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version';if(window.parent) {window.parent.meshSdkPlatform='android';window.parent.meshSdkVersion='$version';}",
            script
        )
    }

    @Test
    fun `test with accessTokens & transferDestinationTokens`() {
        val version = randomString
        val accessTokens = randomString
        val transferDestinationTokens = randomString

        val script = OnPageFinishedScriptBuilder(
            version,
            accessTokens,
            transferDestinationTokens
        ).build()

        assertEquals(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version';if(window.parent) {window.parent.meshSdkPlatform='android';window.parent.meshSdkVersion='$version';};window.accessTokens='$accessTokens';window.transferDestinationTokens='$transferDestinationTokens'",
            script
        )
    }
}