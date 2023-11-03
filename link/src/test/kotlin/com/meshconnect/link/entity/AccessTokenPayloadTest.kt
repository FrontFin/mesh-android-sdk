package com.meshconnect.link.entity

import com.meshconnect.link.randomString
import io.mockk.mockk
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class AccessTokenPayloadTest {
    @Test
    fun `test AccessTokenPayload`() {
        val accountTokens = mockk<List<AccountToken>>()
        val brokerType = randomString
        val brokerName = randomString
        val brokerBrandInfo = mockk<BrandInfo>()
        val expiresInSeconds = 1000
        val refreshTokenExpiresInSeconds = 2000

        val it = AccessTokenPayload(
            accountTokens,
            brokerType,
            brokerName,
            brokerBrandInfo,
            expiresInSeconds,
            refreshTokenExpiresInSeconds
        )

        assertEquals(accountTokens, it.accountTokens)
        assertEquals(brokerType, it.brokerType)
        assertEquals(brokerName, it.brokerName)
        assertEquals(brokerBrandInfo, it.brokerBrandInfo)
        assertEquals(expiresInSeconds, it.expiresInSeconds)
        assertEquals(refreshTokenExpiresInSeconds, it.refreshTokenExpiresInSeconds)
    }

    @Test
    fun `test AccountToken`() {
        val account = mockk<Account>()
        val accessToken = randomString
        val refreshToken = randomString

        val it = AccountToken(account, accessToken, refreshToken)

        assertEquals(account, it.account)
        assertEquals(accessToken, it.accessToken)
        assertEquals(refreshToken, it.refreshToken)
    }

    @Test
    fun `test Account`() {
        val id = randomString
        val name = randomString
        val fund = 1.0
        val cash = 3.0
        val isReconnected = false
        val frontAccountId = randomString

        val it = Account(id, name, frontAccountId, fund, cash, isReconnected)

        assertEquals(id, it.accountId)
        assertEquals(name, it.accountName)
        assertEquals(frontAccountId, it.frontAccountId)
        assertEquals(fund, it.fund)
        assertEquals(cash, it.cash)
        assertEquals(isReconnected, it.isReconnected)
    }

    @Test
    fun `test BrandInfo`() {
        val logo = randomString
        val color = randomString

        val it = BrandInfo(logo, color)

        assertEquals(logo, it.brokerLogo)
        assertEquals(color, it.brokerPrimaryColor)
    }
}