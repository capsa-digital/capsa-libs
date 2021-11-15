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
    @Suppress("FunctionNaming")
    fun testValidator_happyPath() {
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
                it,
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
    @Suppress("FunctionNaming")
    fun testValidator_equal_negative() {
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
        }.then { ex ->
            assertThat(ex).isInstanceOf(AssertionError::class)
            assertThat(ex.message).isEqualTo("XML path //element/data validation failed, document: [#document: null]. Expected <[abcd, 1234]>, actual <[abcd, bcde]>.")
        }
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_like_negative() {
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
        }.then { ex ->
            assertThat(ex).isInstanceOf(AssertionError::class)
            assertThat(ex.message).isEqualTo("expected [XML path //element[id='12345']/data validation failed, document: [#document: null]] to match:</.*ab cd.*/> but was:<\"test1! abcd !1test\">")
        }
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_regex_negative() {
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
        }.then { ex ->
            assertThat(ex).isInstanceOf(AssertionError::class)
            assertThat(ex.message).isEqualTo("expected [XML path //element[id='12345']/data validation failed, document: [#document: null]] to match:</.*ab cd.*/> but was:<\"test1! abcd !1test\">")
        }
    }

    @Test
    @Suppress("FunctionNaming")
    fun testValidator_invalid_path() {
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
        }.then { ex ->
            assertThat(ex).isInstanceOf(Error::class)
            assertThat(ex.message).isEqualTo("Path not found, document: [#document: null], path: //invalid/data")
        }
    }
}