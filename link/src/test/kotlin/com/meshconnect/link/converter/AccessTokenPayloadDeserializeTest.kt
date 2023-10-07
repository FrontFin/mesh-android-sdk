package com.meshconnect.link.converter

import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.Account
import com.meshconnect.link.entity.AccountToken
import com.meshconnect.link.entity.BrandInfo
import com.meshconnect.link.readFile
import org.junit.Test

class AccessTokenPayloadDeserializeTest {

    private val gson = JsonConverter.get()

    @Test
    fun testAccessToken() {
        val json = readFile("access-token.json")
        val actual = gson.fromJson<AccessTokenPayload>(json)
        val expected = AccessTokenPayload(
            brokerType = "alpaca",
            brokerName = "Alpaca",
            brokerBrandInfo = BrandInfo(
                brokerLogo = "iVBORwxxxxxSUhEUgAAA"
            ),
            accountTokens = listOf(
                AccountToken(
                    account = Account(
                        accountId = "2wwwc105-5xx-4390x",
                        accountName = "xx9xxx226",
                        fund = 5.26,
                        cash = 5.26
                    ),
                    accessToken = "8/luwjLWf/QxxfilS2r"
                )
            )
        )
        assert(actual == expected)
    }
}