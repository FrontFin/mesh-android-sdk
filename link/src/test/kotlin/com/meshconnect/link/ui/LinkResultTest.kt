package com.meshconnect.link.ui

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class LinkResultTest {
    // region LinkExit

    @Test
    fun `LinkExit errorMessage is null by default`() {
        LinkExit().errorMessage.shouldBeNull()
    }

    @Test
    fun `LinkExit errorMessage returns the provided string`() {
        LinkExit("something went wrong").errorMessage shouldBeEqualTo "something went wrong"
    }

    @Test
    fun `LinkExit errorMessage is null when null is passed`() {
        LinkExit(null).errorMessage.shouldBeNull()
    }

    // endregion

    // region getLinkResult

    @Test
    fun `getLinkResult returns LinkExit with null message when intent is null`() {
        val result = LinkActivity.getLinkResult(null)
        result.shouldBeInstanceOf<LinkExit>()
        (result as LinkExit).errorMessage.shouldBeNull()
    }

    @Test
    fun `getLinkResult returns LinkExit with message when getParcelable throws`() {
        // Build.VERSION.SDK_INT is 0 in JVM tests (below TIRAMISU), so the deprecated
        // getParcelableExtra(String) overload is called by the inline getParcelable extension.
        val intent =
            mockk<Intent> {
                @Suppress("DEPRECATION")
                every { getParcelableExtra<LinkResult>(any()) } throws RuntimeException("unparcel failure")
            }
        val result = LinkActivity.getLinkResult(intent)
        result.shouldBeInstanceOf<LinkExit>()
        (result as LinkExit).errorMessage shouldBeEqualTo "unparcel failure"
    }

    // endregion
}
