package com.meshconnect.link.usecase

import com.meshconnect.link.EventEmitter
import com.meshconnect.link.UseCaseTest
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BroadcastLinkMessageUseCaseTest: UseCaseTest() {

    private val deserializeToMap = mockk<DeserializeToMap>()
    private val filterLinkMessage = mockk<FilterLinkMessage>()
    private val eventEmitter = mockk<EventEmitter>()

    private val useCase = BroadcastLinkMessageUseCase(
        mainCoroutineRule.dispatcher,
        deserializeToMap,
        filterLinkMessage,
        eventEmitter
    )

    @Test
    fun `test useCase broadcasts the link message`() = runTest {
        every { deserializeToMap(any()) } returns mockk()
        every { filterLinkMessage(any()) } returns mockk()
        coEvery { eventEmitter.emit(any()) } just Runs
        useCase.launch("").apply { assert(isSuccess) }
        coVerify { eventEmitter.emit(any()) }
    }

    @Test
    fun `test useCase does not broadcast invalid message`() = runTest {
        every { deserializeToMap(any()) } returns mockk()
        every { filterLinkMessage(any()) } returns null
        useCase.launch("").apply { assert(isSuccess) }
        coVerify(exactly = 0) { eventEmitter.emit(any()) }
    }

    @Test
    fun `test failure when deserializer throws an exception`() = runTest {
        every { deserializeToMap(any()) } throws mockk()
        useCase.launch("").apply { assert(isFailure) }
    }

    @Test
    fun `test failure when filter throws an exception`() = runTest {
        every { deserializeToMap(any()) } returns mockk()
        every { filterLinkMessage(any()) } throws mockk()
        useCase.launch("").apply { assert(isFailure) }
    }

    @Test
    fun `test failure when broadcast throws an exception`() = runTest {
        every { deserializeToMap(any()) } returns mockk()
        every { filterLinkMessage(any()) } returns mockk()
        coEvery { eventEmitter.emit(any()) } throws mockk()
        useCase.launch("").apply { assert(isFailure) }
    }
}