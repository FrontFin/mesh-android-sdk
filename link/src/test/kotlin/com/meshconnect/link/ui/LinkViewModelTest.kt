package com.meshconnect.link.ui

import com.meshconnect.link.PayloadEmitter
import com.meshconnect.link.ViewModelTest
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.randomString
import com.meshconnect.link.testObserver
import com.meshconnect.link.usecase.BroadcastLinkMessageUseCase
import com.meshconnect.link.usecase.DeserializeLinkMessageUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class LinkViewModelTest : ViewModelTest() {
    private val deserializeLinkMessageUseCase = mockk<DeserializeLinkMessageUseCase>()
    private val payloadEmitter = mockk<PayloadEmitter>()
    private val broadcastLinkMessageUseCase = mockk<BroadcastLinkMessageUseCase>()

    private val viewModel =
        LinkViewModel(
            mainCoroutineRule.dispatcher,
            deserializeLinkMessageUseCase,
            payloadEmitter,
            broadcastLinkMessageUseCase,
        )

    @Test
    fun `test factory`() {
        LinkViewModel.Factory().create(LinkViewModel::class.java)
    }

    @Test
    fun `verify viewModel emits 'done' event`() {
        val json = randomString
        val event = LinkEvent.Done
        val eventObserver = viewModel.linkEvent.testObserver()

        coEvery { deserializeLinkMessageUseCase.launch(json) } returns event
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        viewModel.onJsonReceived(json)

        viewModel.payloads.shouldBeEmpty()
        viewModel.error shouldBe null
        eventObserver.shouldContainEvents(event)
    }

    @Test
    fun `verify viewModel emits error`() =
        runTest {
            val json = randomString
            val th = mockk<Throwable>()
            val errorObserver = viewModel.throwable.testObserver()

            coEvery { deserializeLinkMessageUseCase.launch(json) } throws th
            coEvery { broadcastLinkMessageUseCase.launch(any()) } just Runs
            viewModel.onJsonReceived(json)

            errorObserver.shouldContainEvents(th)
            viewModel.error shouldBe th
        }

    @Test
    fun `verify viewModel emits payload`() =
        runTest {
            val json = randomString
            val payload: LinkPayload = mockk()
            val event = LinkEvent.Payload(payload)
            val eventObserver = viewModel.linkEvent.testObserver()

            coEvery { deserializeLinkMessageUseCase.launch(json) } returns event
            coEvery { payloadEmitter.emit(payload) } just Runs
            coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
            viewModel.onJsonReceived(json)

            viewModel.payloads.shouldContainSame(listOf(payload))
            viewModel.error shouldBe null
            eventObserver.shouldContainEvents(event)
        }

    @Test
    fun `verify viewModel handle null from use case`() {
        val json = randomString
        val eventObserver = viewModel.linkEvent.testObserver()

        coEvery { deserializeLinkMessageUseCase.launch(json) } returns null
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        viewModel.onJsonReceived(json)

        viewModel.payloads.shouldBeEmpty()
        viewModel.error shouldBe null
        eventObserver.shouldBeEmpty()
    }

    @Test
    fun `verify viewModel handle exception from use case`() {
        val json = randomString
        val eventObserver = viewModel.linkEvent.testObserver()
        val msg = randomString
        val exception = IllegalStateException(msg)

        coEvery { deserializeLinkMessageUseCase.launch(json) } throws exception
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        viewModel.onJsonReceived(json)

        viewModel.payloads.shouldBeEmpty()
        viewModel.error shouldBe exception
        eventObserver.shouldBeEmpty()
    }

    @Test
    fun `verify payload is collected before done event when done IO completes first`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val vm =
                LinkViewModel(
                    testDispatcher,
                    deserializeLinkMessageUseCase,
                    payloadEmitter,
                    broadcastLinkMessageUseCase,
                )

            val payloadJson = "payload_json"
            val doneJson = "done_json"
            val payload: LinkPayload = mockk()
            val capturedEvents = mutableListOf<LinkEvent>()
            vm.linkEvent.observeForever { it.consume { e -> capturedEvents.add(e) } }

            // transferFinished IO is slow
            coEvery { deserializeLinkMessageUseCase.launch(payloadJson) } coAnswers {
                delay(100)
                LinkEvent.Payload(payload)
            }
            coEvery { broadcastLinkMessageUseCase.launch(payloadJson) } just Runs
            coEvery { payloadEmitter.emit(payload) } just Runs

            // done IO completes faster — would win the race without the mutex
            coEvery { deserializeLinkMessageUseCase.launch(doneJson) } coAnswers {
                delay(10)
                LinkEvent.Done
            }
            coEvery { broadcastLinkMessageUseCase.launch(doneJson) } just Runs

            vm.onJsonReceived(payloadJson)
            vm.onJsonReceived(doneJson)

            advanceUntilIdle()

            vm.payloads.shouldContainSame(listOf(payload))
            // Payload must be emitted before Done — order matters
            capturedEvents shouldBeEqualTo listOf(LinkEvent.Payload(payload), LinkEvent.Done)
        }

    @Test
    fun `verify viewModel handle exception inside success block`() {
        val json = randomString
        val eventObserver = viewModel.linkEvent.testObserver()
        val event = LinkEvent.Payload(mockk())
        val msg = randomString
        val exception = IllegalStateException(msg)

        coEvery { deserializeLinkMessageUseCase.launch(json) } returns event
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        coEvery { payloadEmitter.emit(event.payload) } throws exception
        viewModel.onJsonReceived(json)

        viewModel.error shouldBe exception
        eventObserver.shouldBeEmpty()
    }
}
