package digital.capsa.it.xml

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import digital.capsa.it.gherkin.given
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
@Tag("unit")
class XmlPathValidatorTest {

    @Test
    fun `Validator - Happy path`() {
        given {
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
               <element>
                  <data>test1! abcd !1test</data>
                  <id>12345</id>
               </element>
               <element>
                  <data>test2@ bcde @2test</data>
                  <id>23456</id>
               </element>
            </root>
            """.trimIndent()
        }.on {
            XmlPathValidator.assertXml(
                this,
                listOf(
                    ValidationRule("//element/data", OpType.equal, listOf("test1! abcd !1test", "test2@ bcde @2test")),
                    ValidationRule("//element[id='12345']/data", OpType.equal, "test1! abcd !1test"),
                    ValidationRule("//element[id='12345']/data", OpType.like, "abcd"),
                    ValidationRule("//element[id='12345']/data", OpType.regex, ".*abcd.*")
                )
            )
        }.then {
            //No exception thrown
        }
    }

    @Test
    fun `Validator - equal negative`() {
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
            """.trimIndent()
        }.onError {
            XmlPathValidator.assertXml(
                it,
                listOf(
                    ValidationRule("//element/data", OpType.equal, listOf("abcd", "1234"))
                )
            )
        }.then {
            assertThat(this).isInstanceOf(AssertionError::class)
            assertThat(message).isEqualTo("XML path //element/data validation failed, document: [#document: null]. Expected <[abcd, 1234]>, actual <[abcd, bcde]>.")
        }
    }

    @Test
    fun `Validator - like negative`() {
        given {
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
               <element>
                  <data>test1! abcd !1test</data>
                  <id>12345</id>
               </element>
               <element>
                  <data>test2@ bcde @2test</data>
                  <id>23456</id>
               </element>
            </root>
            """.trimIndent()
        }.onError {
            XmlPathValidator.assertXml(
                it,
                listOf(
                    ValidationRule("//element[id='12345']/data", OpType.like, "ab cd")
                )
            )
        }.then {
            assertThat(this).isInstanceOf(AssertionError::class)
            assertThat(message).isEqualTo("expected [XML path //element[id='12345']/data validation failed, document: [#document: null]] to match:</.*ab cd.*/> but was:<\"test1! abcd !1test\">")
        }
    }

    @Test
    fun `Validator - regex negative`() {
        given {
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
               <element>
                  <data>test1! abcd !1test</data>
                  <id>12345</id>
               </element>
               <element>
                  <data>test2@ bcde @2test</data>
                  <id>23456</id>
               </element>
            </root>
            """.trimIndent()
        }.onError {
            XmlPathValidator.assertXml(
                it,
                listOf(
                    ValidationRule("//element[id='12345']/data", OpType.regex, ".*ab cd.*")
                )
            )
        }.then {
            assertThat(this).isInstanceOf(AssertionError::class)
            assertThat(message).isEqualTo("expected [XML path //element[id='12345']/data validation failed, document: [#document: null]] to match:</.*ab cd.*/> but was:<\"test1! abcd !1test\">")
        }
    }

    @Test
    fun `Validator - invalid path`() {
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
            """.trimIndent()
        }.onError {
            XmlPathValidator.assertXml(
                it,
                listOf(
                    ValidationRule("//invalid/data", OpType.equal, listOf("abcd", "bcde"))
                )
            )
        }.then {
            assertThat(this).isInstanceOf(Error::class)
            assertThat(message).isEqualTo("Path not found, document: [#document: null], path: //invalid/data")
        }
    }
}