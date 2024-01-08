package com.meshconnect.link.converter

import com.meshconnect.link.entity.BrandInfo
import com.meshconnect.link.entity.DelayedAuthPayload
import com.meshconnect.link.readFile
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class DelayedAuthPayloadDeserializeTest {

    private val gson = JsonConverter.get()

    @Test
    fun `deserialize 'delayedAuthentication'`() {
        val json = readFile("delayed-auth-payload.json")
        val actual = gson.fromJson<DelayedAuthPayload>(json)
        val expected = DelayedAuthPayload(
            refreshTokenExpiresInSeconds = 1923849,
            brokerType = "alpaca",
            refreshToken = "lorem-ipsum",
            brokerName =  "Alpacas",
            brokerBrandInfo = BrandInfo(
                brokerLogo = "iVB2RSUhEUgAAA",
                brokerPrimaryColor =  "#FFF"
            )
        )
        assertEquals(expected, actual)
    }
}