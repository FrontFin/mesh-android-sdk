package com.meshconnect.link.usecase

import com.meshconnect.link.EventEmitter
import com.meshconnect.link.UseCaseTest
import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.randomString
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BroadcastLinkMessageUseCaseTest: UseCaseTest() {

    private val jsonConverter = mockk<JsonConverter>()
    private val filterLinkMessage = mockk<FilterLinkMessage>()
    private val eventEmitter = mockk<EventEmitter>()

    private val useCase = BroadcastLinkMessageUseCase(
        jsonConverter,
        filterLinkMessage,
        eventEmitter
    )

    private fun mockkDeserializeToMap(inputValue: String = mockk(), returnValue: Map<String, *>) {
        every { jsonConverter.toMap(inputValue) } returns returnValue
    }

    private fun mockkFilterLinkMessage(inputValue: Map<String, *> = mockk(), returnValue: Map<String, *>?) {
        every { filterLinkMessage.filter(inputValue) } returns returnValue
    }

    @Test
    fun `test useCase broadcasts the message correctly`() = runTest {
        val json = randomString
        val convertedMap: Map<String, *> = mockk()
        val filteredMap: Map<String, *> = mockk()

        mockkDeserializeToMap(json, convertedMap)
        mockkFilterLinkMessage(convertedMap, filteredMap)
        coEvery { eventEmitter.emit(filteredMap) } just Runs

        useCase.launch(json)

        verify(exactly = 1) {
            jsonConverter.toMap(json)
            filterLinkMessage.filter(convertedMap)
        }
        coVerify(exactly = 1) {
            eventEmitter.emit(filteredMap)
        }
    }

    @Test
    fun `test useCase does not broadcast a map on filtered out map`() = runTest {
        val json = randomString
        val convertedMap: Map<String, *> = mockk()
        val filteredMap: Map<String, *>? = null

        mockkDeserializeToMap(json, convertedMap)
        mockkFilterLinkMessage(convertedMap, filteredMap)

        useCase.launch(json)

        verify(exactly = 1) {
            jsonConverter.toMap(json)
            filterLinkMessage.filter(convertedMap)
        }
        coVerify(exactly = 0) {
            eventEmitter.emit(any())
        }
    }
}