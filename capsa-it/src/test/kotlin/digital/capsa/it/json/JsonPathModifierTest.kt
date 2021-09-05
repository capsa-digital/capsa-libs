package digital.capsa.it.json

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class JsonPathModifierTest {

    @Test
    @Suppress("FunctionNaming")
    fun testModifier_happyPath() {
        assertEquals(
            """[{"id":"12345","data":"qwert","num":12345},{"id":"23456","data":"asdfg","num":23456}]""",
            JsonPathModifier.modifyJson(
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
        """.trimIndent(), mapOf(
                    "@[?(@.id == '12345')].data" to "qwert",
                    "@[?(@.id == '23456')].data" to "asdfg"
                )
            )
        )
    }

    @Test
    @Suppress("FunctionNaming")
    fun testModifier_date() {
        assertEquals(
            """[{"id":"12345","endDate":[2019,7,30,23,59]}]""",
            JsonPathModifier.modifyJson(
                """
            [{
              "id": "12345",
              "endDate": [
                  2019,
                  7,
                  31,
                  23,
                  59
                ]
            }]
        """.trimIndent(), mapOf("@[?(@.id == '12345')].endDate" to arrayOf(2019, 7, 30, 23, 59))
            )
        )
    }
}