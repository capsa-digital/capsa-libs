package digital.capsa.it.gherkin

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class GherkinTest {

    @Test
    fun `happy path`() {
        given {
            "abc"
        }.on {
            assertEquals("abc", this)
            56
        }.then {
            assertEquals(56, this)
        }
    }

    @Test
    fun `happy path without given`() {
        on {
            58
        }.then {
            assertEquals(58, this)
        }
    }

    @Test
    fun `exception test`() {
        given {
            "def"
        }.onError {
            throw RuntimeException("Exception $it")
        }.then {
            assertEquals("Exception def", message)
        }
    }

    @Test
    fun `exception test without given`() {
        onError {
            throw RuntimeException("Exception cde")
        }.then {
            assertEquals("Exception cde", message)
        }
    }

    @Test
    fun `exception test - no exception thrown`() {
        given {
            "def"
        }.onError {
            "abc"
        }.then {
            assertEquals("No error encountered", message)
        }
    }
}