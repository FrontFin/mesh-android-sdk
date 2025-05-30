package com.meshconnect.link.converter

import com.meshconnect.link.entity.BrandInfo
import com.meshconnect.link.entity.DelayedAuthPayload
import com.meshconnect.link.readFile
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class DelayedAuthPayloadDeserializeTest {
    private val jsonConverter = JsonConverter

    @Test
    fun `deserialize 'delayedAuthentication'`() {
        val json = readFile("delayed-auth-payload.json")
        val actual = jsonConverter.fromJson(json, DelayedAuthPayload::class.java)
        val expected =
            DelayedAuthPayload(
                refreshTokenExpiresInSeconds = 1923849,
                brokerType = "alpaca",
                refreshToken = "lorem-ipsum",
                brokerName = "Alpacas",
                brokerBrandInfo =
                    BrandInfo(
                        brokerLogo = "iVB2RSUhEUgAAA",
                        brokerPrimaryColor = "#FFF",
                        logoLightUrl = "http://logoLightUrl",
                        logoDarkUrl = "http://logoDarkUrl",
                        iconLightUrl = "http://iconLightUrl",
                        iconDarkUrl = "http://iconDarkUrl",
                    ),
            )
        assertEquals(expected, actual)
    }
}
