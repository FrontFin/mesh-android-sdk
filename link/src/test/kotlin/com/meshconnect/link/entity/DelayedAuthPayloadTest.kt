package com.meshconnect.link.entity

import com.meshconnect.link.randomInt
import com.meshconnect.link.randomString
import io.mockk.mockk
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class DelayedAuthPayloadTest {

    @Test
    fun `test 'DelayedAuthPayload' entity`() {
        val refreshTokenExpiresInSeconds: Int = randomInt
        val brokerType: String = randomString
        val refreshToken: String = randomString
        val brokerName: String = randomString
        val brokerBrandInfo: BrandInfo = mockk()

        val it = DelayedAuthPayload(
            refreshTokenExpiresInSeconds,
            brokerType,
            refreshToken, brokerName, brokerBrandInfo
        )

        assertEquals(refreshTokenExpiresInSeconds, it.refreshTokenExpiresInSeconds)
        assertEquals(brokerType, it.brokerType)
        assertEquals(brokerName, it.brokerName)
        assertEquals(refreshToken, it.refreshToken)
        assertEquals(brokerBrandInfo, it.brokerBrandInfo)
    }
}