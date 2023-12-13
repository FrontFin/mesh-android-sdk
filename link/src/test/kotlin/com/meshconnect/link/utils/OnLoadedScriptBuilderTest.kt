package com.meshconnect.link.utils

import com.meshconnect.link.randomString
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class OnLoadedScriptBuilderTest {
    @Test
    fun `test default constructor`() {
        val version = randomString
        val script = OnLoadedScriptBuilder(version).build()
        assertEquals(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version'",
            script
        )
    }

    @Test
    fun `test with accessTokens & transferDestinationTokens`() {
        val version = randomString
        val accessTokens = randomString
        val transferDestinationTokens = randomString

        val script = OnLoadedScriptBuilder(
            version,
            accessTokens,
            transferDestinationTokens
        ).build()

        assertEquals(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version';window.accessTokens='$accessTokens';window.transferDestinationTokens='$transferDestinationTokens'",
            script
        )
    }
}