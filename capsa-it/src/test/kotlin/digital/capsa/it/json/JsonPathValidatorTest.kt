package digital.capsa.it.json

import digital.capsa.it.gherkin.given
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@Suppress("MaxLineLength")
@Tag("unit")
class JsonPathValidatorTest {

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_happyPath() {
        given {
            """
                [{
                  "id": "12345",
                  "data": "abcd",
                  "num": 12345
                }, {
                  "id": "23456",
                  "data": "bcde",
                  "num": 23456
                }]
            """
        }.on {
            it.trimIndent()
        }.then {
            JsonPathValidator.assertJson(it, listOf(
                    ValidationRule("$.*.id", OpType.equal, listOf("12345", "23456")),
                    ValidationRule("@[?(@.id == '12345')].data", OpType.equal, "abcd"),
                    ValidationRule("@[?(@.id == '23456')].num", OpType.equal, 23456)
            ))
        }
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_empty() {
        JsonPathValidator.assertJson("""
            []
        """.trimIndent(), listOf(
                ValidationRule("$.*.id", OpType.equal, emptyList<String>())
        ))
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_empty_negative() {
        var exception: AssertionError? = null
        try {
            JsonPathValidator.assertJson("""
            [{
              "id": "12345"
            }, {
              "id": "23456"
            }]
        """.trimIndent(), listOf(
                    ValidationRule("$.*.id", OpType.equal, "")
            )
            )
        } catch (e: AssertionError) {
            exception = e
        }
        assertTrue(exception is AssertionError)
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_regex() {
        JsonPathValidator.assertJson("""
            {
              "id": "12345"
            }
        """.trimIndent(), listOf(
                ValidationRule("$.id", OpType.regex, ".*")
        ))
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_like() {
        JsonPathValidator.assertJson("""
            {
              "cause": "Cannot deserialize value of type `java.util.UUID` from String \"a2f674455-e5f4-4946-a19a-xdace6e1a598\": UUID has to be represented by standard 36-char representation\n at [Source: (String)\"{\"region\":\"qc\",\"listOfId\":[\"a2f674455-e5f4-4946-a19a-xdace6e1a598\"]}\"; line: 1, column: 28]"
            }
        """.trimIndent(), listOf(
                ValidationRule("$.cause", OpType.like, "Cannot deserialize value of type"),
                ValidationRule("$.cause", OpType.like, "UUID has to be represented by standard 36-char representation")
        ))
    }
}