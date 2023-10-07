package com.meshconnect.link

import org.junit.Rule

open class UseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainDispatcherRule()
}