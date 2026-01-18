package com.lampcontrol.models

import com.lampcontrol.api.models.Error
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ErrorModelTest {
    @Test
    fun `error model stores message`() {
        val e = Error(error = "something_wrong")
        assertEquals("something_wrong", e.error)
        assertNotNull(e.toString())
    }
}
