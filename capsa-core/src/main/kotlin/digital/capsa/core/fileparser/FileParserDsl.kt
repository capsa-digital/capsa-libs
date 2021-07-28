package digital.capsa.core.fileparser

import java.io.BufferedReader

@DslMarker
annotation class ParserMarker

@ParserMarker
class Parser(private val bufferedReader: BufferedReader) {

    private val lines: List<Line> = bufferedReader.lineSequence().map {
        Line(it, null)
    }.toMutableList().also {
        bufferedReader.close()
    }

    fun header(
            recordBuilder: TransformerBuilder.() -> Any
    ) {
        lines[0].obj = TransformerBuilder(lines[0].str, 0).recordBuilder()
    }

    fun line(
            recordBuilder: TransformerBuilder.() -> Any
    ) {
        lines.forEachIndexed { index, line ->
            if (index != 0) {
                lines[index].obj = TransformerBuilder(line.str, index).recordBuilder()
            }
        }
    }

    fun getRecords(): List<Any?> {
        return lines.map { it.obj }
    }

    private data class Line(
            val str: String,
            var obj: Any?
    )
}

class TransformerBuilder(
        val line: String,
        val index: Int
) {
    inline fun <reified R> field(
            name: String? = null,
            from: Int,
            toExclusive: Int,
            default: () -> R? = { null },
            noinline parser: ((String) -> R?)? = null
    ): R? {
        val str = readField(from, toExclusive)
        return if (str.isNotBlank()) {
            parser?.let { parser(str) } ?: defaultTypeParser<R>(str)
        } else default.invoke()
    }

    inline fun <reified R> mandatoryField(
            name: String? = null,
            from: Int,
            toExclusive: Int,
            default: () -> R = {
                throw FileParserException(name?.let { "The field $name is mandatory" } ?: "Field is mandatory")
            },
            noinline parser: ((String) -> R)? = null
    ): R {
        val str = readField(from, toExclusive)
        return if (str.isNotBlank()) {
            parser?.let { parser(str) } ?: defaultTypeParser(str)
        } else default.invoke()
    }

    fun readField(from: Int,
                  toExclusive: Int): String {
        return if (line.length >= toExclusive) line.substring(from, toExclusive).trim() else ""
    }
}

fun parser(
        bufferedReader: BufferedReader,
        init: Parser.() -> Unit
): Parser {
    val parser = Parser(bufferedReader)
    parser.init()
    return parser
}

open class FileParserException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)