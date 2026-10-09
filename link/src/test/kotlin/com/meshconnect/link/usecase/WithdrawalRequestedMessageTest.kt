package com.meshconnect.link.usecase

import com.meshconnect.link.EventEmitter
import com.meshconnect.link.LinkEvents
import com.meshconnect.link.UseCaseTest
import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.readFile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class WithdrawalRequestedMessageTest : UseCaseTest() {
    private val broadcast = BroadcastLinkMessageUseCase(JsonConverter, FilterLinkMessage, EventEmitter())

    private fun broadcastAndCollect(json: String): List<Map<String, *>> {
        val received = mutableListOf<Map<String, *>>()
        runTest {
            val collector = launch(UnconfinedTestDispatcher(testScheduler)) { LinkEvents.toList(received) }
            broadcast.launch(json)
            collector.cancel()
        }
        return received
    }

    @Test
    fun `withdrawalRequested from Link reaches LinkEvents unrenamed`() {
        broadcastAndCollect(readFile("withdrawal-requested.json")) shouldBeEqualTo
            listOf(
                mapOf(
                    "type" to "withdrawalRequested",
                    "payload" to mapOf("transferId" to "transfer-1", "status" to "pending"),
                ),
            )
    }

    @Test
    fun `withdrawalRequested with an unknown status reaches LinkEvents unchanged`() {
        val json = """{"type":"withdrawalRequested","payload":{"transferId":"transfer-1","status":"failed"}}"""

        broadcastAndCollect(json) shouldBeEqualTo
            listOf(
                mapOf(
                    "type" to "withdrawalRequested",
                    "payload" to mapOf("transferId" to "transfer-1", "status" to "failed"),
                ),
            )
    }

    @Test
    fun `withdrawalRequested adds no payload to the Link result`() =
        runTest {
            DeserializeLinkMessageUseCase(JsonConverter).launch(readFile("withdrawal-requested.json")).shouldBeNull()
        }

    @Test
    fun `transferFinished still reaches LinkEvents as transferCompleted`() {
        val events = broadcastAndCollect(readFile("transfer-error.json").let { """{"type":"transferFinished","payload":$it}""" })

        events.single()["type"] shouldBeEqualTo "transferCompleted"
    }
}
