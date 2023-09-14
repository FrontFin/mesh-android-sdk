package com.getfront.catalog.ui

import com.getfront.catalog.ViewModelTest
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.store.SendPayloadToReceiverUseCase
import com.getfront.catalog.testObserver
import com.getfront.catalog.usecase.GetLinkEventUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class FrontCatalogViewModelTest : ViewModelTest() {

    private val getLinkEventUseCase = mockk<GetLinkEventUseCase>()
    private val sendPayloadToReceiverUseCase = mockk<SendPayloadToReceiverUseCase>()
    private val viewModel = FrontCatalogViewModel(getLinkEventUseCase, sendPayloadToReceiverUseCase)

    @Test
    fun `test onJsonReceived received failure`() = runTest {
        val th = mockk<Throwable>()
        val errorObserver = viewModel.throwable.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.failure(th)
        viewModel.onJsonReceived("")

        errorObserver.shouldContainEvents(th)
        assert(viewModel.error == th)
    }

    @Test
    fun `test onJsonReceived received payload`() = runTest {
        val payload = mockk<LinkEvent.Payload> {
            every { payload } returns mockk()
        }
        val eventObserver = viewModel.linkEvent.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.success(payload)
        coEvery { sendPayloadToReceiverUseCase.launch(payload.payload) } just Runs
        viewModel.onJsonReceived("")

        viewModel.payloads.shouldContainSame(listOf(payload.payload))
        assert(viewModel.error == null)
        eventObserver.shouldContainEvents(payload)
    }

    @Test
    fun `test onJsonReceived other event`() {
        val event = LinkEvent.Done
        val eventObserver = viewModel.linkEvent.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.success(event)
        viewModel.onJsonReceived("")

        viewModel.payloads.shouldBeEmpty()
        assert(viewModel.error == null)
        eventObserver.shouldContainEvents(event)
    }

    @Test
    fun `test factory`() {
        FrontCatalogViewModel.Factory().create(FrontCatalogViewModel::class.java)
    }
}