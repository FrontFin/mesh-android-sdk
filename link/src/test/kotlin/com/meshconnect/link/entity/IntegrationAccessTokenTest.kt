package com.meshconnect.link.entity

import com.meshconnect.link.randomString
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class IntegrationAccessTokenTest {
    @Test
    fun `test IntegrationAccessToken`() {
        val accountId: String = randomString
        val accountName: String = randomString
        val accessToken: String = randomString
        val brokerType: String = randomString
        val brokerName: String = randomString

        val it =
            IntegrationAccessToken(
                accountId,
                accountName,
                accessToken,
                brokerType,
                brokerName,
            )

        assertEquals(accountId, it.accountId)
        assertEquals(accountName, it.accountName)
        assertEquals(accessToken, it.accessToken)
        assertEquals(brokerType, it.brokerType)
        assertEquals(brokerName, it.brokerName)
    }
}
