package digital.capsa.core.fileparser

import java.io.BufferedReader

@DslMarker
annotation class ParserMarker

@ParserMarker
class Parser(private val bufferedReader: BufferedReader) {

    private var lineCount: Int = 0

    private val lines: List<Line> = bufferedReader.lineSequence().map {
        Line(it, null)
    }.toMutableList().also {
        bufferedReader.close()
    }

    fun header(
        recordBuilder: TransformerBuilder.() -> Any
    ) {
        val headerIndex = lineCount++
        lines[headerIndex].obj = TransformerBuilder(lines[headerIndex].str, headerIndex).recordBuilder()
    }

    fun line(
        recordBuilder: TransformerBuilder.() -> Any
    ) {
        while (lineCount < lines.size) {
            val lineIndex = lineCount++
            lines[lineIndex].obj = TransformerBuilder(lines[lineIndex].str, lineIndex).recordBuilder()
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
    private val line: String,
    val index: Int

) {
    inline fun <reified R> field(
        from: Int,
        toExclusive: Int,
        name: String? = null,
        default: () -> R? = { null },
        noinline parser: ((String) -> R?)? = null
    ): R? {
        val str = readField(from, toExclusive)
        return if (str.isNotBlank()) {
            parser?.let { parser(str) } ?: defaultTypeParser<R>(str)
        } else default.invoke()
    }

    inline fun <reified R> mandatoryField(
        from: Int,
        toExclusive: Int,
        name: String? = null,
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