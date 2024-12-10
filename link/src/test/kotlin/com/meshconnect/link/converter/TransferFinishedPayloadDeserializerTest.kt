package com.meshconnect.link.converter

import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.readFile
import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertThrows
import org.junit.Test

class TransferFinishedPayloadDeserializerTest {

    private val gson = JsonConverter.get()

    @Test
    fun `test success payload`() {
        val json = readFile("transfer-success.json")
        val actual = gson.fromJson<TransferFinishedPayload>(json)
        val expected = TransferFinishedSuccessPayload(
            txId = "234sdf-xxx3902",
            fromAddress = "0923xxx",
            toAddress = "92811yyyy",
            symbol = "UST",
            amount = 1.0024,
            networkId = "79823981e",
            amountInFiat = 10.55,
            totalAmountInFiat = 11.2,
            networkName = "Polygon",
            txHash = "1-9340-194350432-21123232"
        )
        assert(actual == expected)
    }

    @Test
    fun `test success payload with empty address`() {
        val json = readFile("transfer-success-empty-fields.json")
        val actual = gson.fromJson<TransferFinishedPayload>(json)
        val expected = TransferFinishedSuccessPayload(
            txId = "234sdf-xxx3902",
            fromAddress = "",
            toAddress = "",
            symbol = "UST",
            amount = 1.0024,
            networkId = "79823981e",
            txHash = "",
            networkName = "",
            amountInFiat = 0.0,
            totalAmountInFiat = 0.0
        )
        assert(actual == expected)
    }

    @Test
    fun `test error payload`() {
        val json = readFile("transfer-error.json")
        val actual = gson.fromJson<TransferFinishedPayload>(json)
        val expected = TransferFinishedErrorPayload(
            errorMessage = "service unavailable"
        )
        assert(actual == expected)
    }

    @Test
    fun `test invalid status`() {
        val json = "{status:'lorem'}"
        val ex = assertThrows(JsonSyntaxException::class.java) {
            gson.fromJson<TransferFinishedPayload>(json)
        }
        assert(ex.message == "java.lang.IllegalStateException: unknown status 'lorem'")
    }
}
