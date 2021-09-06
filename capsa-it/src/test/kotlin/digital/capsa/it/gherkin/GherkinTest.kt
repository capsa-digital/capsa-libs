package digital.capsa.it.gherkin

import kotlin.test.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class GherkinTest {

    @Test
    fun `happy path`() {
        given {
            "abc"
        }.on {
            assertEquals("abc", it)
            56
        }.then {
            assertEquals(56, it)
        }
    }

    @Test
    fun `happy path without given`() {
        on {
            58
        }.then {
            assertEquals(58, it)
        }
    }

    @Test
    fun `exception test`() {
        given {
            "def"
        }.onError {
            throw RuntimeException("Exception $it")
        }.then {
            assertEquals("Exception def", it.message)
        }
    }

    @Test
    fun `exception test without given`() {
        onError {
            throw RuntimeException("Exception cde")
        }.then {
            assertEquals("Exception cde", it.message)
        }
    }

    @Test
    fun `exception test - no exception thrown`() {
        given {
            "def"
        }.onError {
            "abc"
        }.then {
            assertEquals("No error encountered", it.message)
        }
    }
}