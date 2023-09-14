package com.getfront.catalog

import org.junit.Rule

open class UseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainDispatcherRule()
}