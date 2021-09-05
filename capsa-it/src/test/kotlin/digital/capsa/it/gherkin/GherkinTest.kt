package digital.capsa.it.gherkin

import digital.capsa.core.logger
import kotlin.test.assertEquals
import org.junit.jupiter.api.Disabled
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
    @Disabled
    fun `exception test`() {
        given {
            "def"
        }.on {
            throw RuntimeException("Exception $it")
        }.then {
            logger.info("then $it")
        }
    }
}