package com.lampcontrol.di

import kotlin.test.*
import org.junit.jupiter.api.Test

class ServiceContainerTest {
    @Test
    fun `lampService is lazy singleton`() {
        val s1 = ServiceContainer.lampService
        val s2 = ServiceContainer.lampService
        assertNotNull(s1)
        assertSame(s1, s2)
    }
}
