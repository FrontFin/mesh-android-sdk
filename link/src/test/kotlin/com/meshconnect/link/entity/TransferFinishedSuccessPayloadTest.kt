package com.meshconnect.link.entity

import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class TransferFinishedSuccessPayloadTest {
    @Test
    fun `test TransferFinishedSuccessPayload`() {
        TransferFinishedSuccessPayload(
            txId = "234sdf-xxx3902",
            fromAddress = "0923xxx",
            toAddress = "92811yyyy",
            symbol = "UST",
            amount = 1.0024,
            networkId = "79823981e",
            amountInFiat = 12.0,
            totalAmountInFiat = 112.0,
            networkName = "Solana",
            txHash = "o4398590211",
            transferId = "trid",
            refundAddress = "ra",
        ).apply {
            assertEquals("234sdf-xxx3902", txId)
            assertEquals("0923xxx", fromAddress)
            assertEquals("92811yyyy", toAddress)
            assertEquals("UST", symbol)
            assertEquals(1.0024, amount)
            assertEquals("79823981e", networkId)
            assertEquals(12.0, amountInFiat)
            assertEquals(112.0, totalAmountInFiat)
            assertEquals("Solana", networkName)
            assertEquals("o4398590211", txHash)
            assertEquals("trid", transferId)
            assertEquals("ra", refundAddress)
        }
    }
}
