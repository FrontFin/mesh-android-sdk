package com.getfront.catalog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

open class ViewModelTest : UseCaseTest() {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
}