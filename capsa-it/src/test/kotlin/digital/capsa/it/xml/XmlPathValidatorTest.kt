package digital.capsa.it.xml

import digital.capsa.it.gherkin.given
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
@Tag("unit")
class XmlPathValidatorTest {

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_happyPath() {
        given {
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
               <element>
                  <data>abcd</data>
                  <id>12345</id>
               </element>
               <element>
                  <data>bcde</data>
                  <id>23456</id>
               </element>
            </root>
            """
        }.on {
            it.trimIndent()
        }.then {
            XmlPathValidator.assertXml(
                it, listOf(
                    ValidationRule("//element/data", OpType.equal, listOf("abcd", "bcde")),
                    ValidationRule("//element[id='12345']/data", OpType.equal, "abcd")
                )
            )
        }
    }
}