package com.getfront.catalog.converter

import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.readFile
import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertThrows
import org.junit.Test

class TransferFinishedPayloadDeserializerTest {

    private val gson = JsonConverter.get()

    @Test
    fun testSuccess() {
        val json = readFile("transfer-success.json")
        val actual = gson.fromJson<TransferFinishedPayload>(json)
        val expected = TransferFinishedSuccessPayload(
            txId = "234sdf-xxx3902",
            fromAddress = "0923xxx",
            toAddress = "92811yyyy",
            symbol = "UST",
            amount = 1.0024,
            networkId = "79823981e"
        )
        assert(actual == expected)
    }

    @Test
    fun testError() {
        val json = readFile("transfer-error.json")
        val actual = gson.fromJson<TransferFinishedPayload>(json)
        val expected = TransferFinishedErrorPayload(
            errorMessage = "service unavailable"
        )
        assert(actual == expected)
    }

    @Test
    fun testNull() {
        val json = "{status:'lorem'}"
        val ex = assertThrows(JsonSyntaxException::class.java) {
            gson.fromJson<TransferFinishedPayload>(json)
        }
        assert(ex.message == "java.lang.IllegalStateException: unknown status 'lorem'")
    }
}
