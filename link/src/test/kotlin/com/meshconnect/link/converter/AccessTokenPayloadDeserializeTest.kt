package com.meshconnect.link.converter

import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.Account
import com.meshconnect.link.entity.AccountToken
import com.meshconnect.link.entity.BrandInfo
import com.meshconnect.link.readFile
import org.junit.Test

class AccessTokenPayloadDeserializeTest {

    private val jsonConverter = JsonConverter

    @Test
    fun testAccessToken() {
        val json = readFile("access-token.json")
        val actual = jsonConverter.fromJson(json, AccessTokenPayload::class.java)
        val expected = AccessTokenPayload(
            brokerType = "alpaca",
            brokerName = "Alpaca",
            brokerBrandInfo = BrandInfo(
                brokerLogo = "iVBORwxxxxxSUhEUgAAA",
                brokerPrimaryColor = "#FFF"
            ),
            expiresInSeconds = 200,
            refreshTokenExpiresInSeconds = 300,
            accountTokens = listOf(
                AccountToken(
                    account = Account(
                        accountId = "2wwwc105-5xx-4390x",
                        accountName = "xx9xxx226",
                        fund = 5.36,
                        cash = 5.26,
                        isReconnected = false,
                        frontAccountId = "wow_id"
                    ),
                    accessToken = "8/luwjLWf/QxxfilS2r",
                    refreshToken = "#34381lmefl93"
                )
            )
        )
        assert(actual == expected)
    }
}