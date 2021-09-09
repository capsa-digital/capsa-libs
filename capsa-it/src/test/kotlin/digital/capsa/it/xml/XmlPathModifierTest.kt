package digital.capsa.it.xml

import kotlin.test.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class XmlPathModifierTest {

    @Test
    @Suppress("FunctionNaming")
    fun testModifier_happyPath() {
        assertEquals(
            removeWhiteSpaces(
                """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <root>
                   <element>
                      <data>qwert</data>
                      <id>12345</id>
                      <num>12345</num>
                   </element>
                   <element>
                      <data>asdfg</data>
                      <id>23456</id>
                      <num>23456</num>
                   </element>
                </root>
                """.trimIndent()
            ),
            removeWhiteSpaces(
                XmlPathModifier.modifyXml(
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <root>
                       <element>
                          <data>abcd</data>
                          <id>12345</id>
                          <num>12345</num>
                       </element>
                       <element>
                          <data>bcde</data>
                          <id>23456</id>
                          <num>23456</num>
                       </element>
                    </root>
                    """.trimIndent(), mapOf(
                    "//element[id='12345']/data" to "qwert",
                    "//element[id='23456']/data" to "asdfg"
                )
                )
            )
        )
    }

    private fun removeWhiteSpaces(input: String): String {
        return input.replace("\\s+".toRegex(), "")
    }
}