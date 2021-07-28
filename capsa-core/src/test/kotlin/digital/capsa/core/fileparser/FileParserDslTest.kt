package digital.capsa.core.fileparser

import java.io.BufferedReader
import java.io.StringReader
import java.time.LocalDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FileParserDslTest {

    data class Header(val string1: String?, val int1: Int?, val date1: LocalDate?)

    data class Item(val string1: String?, val string2: String?, val int1: Int?, val date1: LocalDate?)

    @Test
    fun test1() {
        val parser = parser(
                BufferedReader(
                        StringReader(
                                """
                    bbb 23 2021-03-09
                    bbb cc 23 2021-04-19
     """.trimIndent()
                        )
                )
        ) {
            header {
                Header(
                        field(0, 4, "field1"),
                        field(4, 7, "field2"),
                        field(7, 17, "field3")
                )
            }
            line {
                Item(
                        field(0, 4, "field1"),
                        field(4, 7, "field2"),
                        field(7, 10, "field3"),
                        field(10, 20, "field4")
                )
            }
        }
        val header = parser.getRecords()[0] as Header
        Assertions.assertEquals("bbb", header.string1)
        Assertions.assertEquals(23, header.int1)
        Assertions.assertEquals(LocalDate.parse("2021-03-09"), header.date1)

        val item = parser.getRecords()[1] as Item
        Assertions.assertEquals("bbb", item.string1)
        Assertions.assertEquals("cc", item.string2)
        Assertions.assertEquals(23, item.int1)
        Assertions.assertEquals(LocalDate.parse("2021-04-19"), item.date1)
    }

}


