package digital.capsa.core.fileparser

import java.io.BufferedReader

@DslMarker
annotation class ParserMarker

@ParserMarker
class Parser(private val bufferedReader: BufferedReader) {

    private var lineCount: Int = 0

    private val records: List<Record> = bufferedReader.lineSequence().map {
        Record(it)
    }.toMutableList().also {
        bufferedReader.close()
    }

    fun header(
        length: Int? = null,
        recordBuilder: TransformerBuilder.() -> Any
    ) {
        if (lineCount < records.size) {
            processLine(lineCount++, length, recordBuilder)
        }
    }

    fun line(
        length: Int? = null,
        recordBuilder: TransformerBuilder.() -> Any
    ) {
        while (lineCount < records.size) {
            processLine(lineCount++, length, recordBuilder)
        }
    }

    private fun processLine(lineIndex: Int, length: Int?, recordBuilder: TransformerBuilder.() -> Any) {
        try {
            length?.let {
                if (records[lineIndex].str.length != it) throw FileParserException("Line length should be $length but was ${records[lineIndex].str.length}")
            }
            val builder = TransformerBuilder(records[lineIndex].str, lineIndex)
            records[lineIndex].value = builder.recordBuilder()
        } catch (e: Exception) {
            if (e is FileParserException) {
                records[lineIndex].error = e
            } else {
                records[lineIndex].error = FileParserException("Unknown parser exception", e)
            }
        }
    }

    fun getRecords(): List<Record> {
        return records.toList()
    }
}

data class Record(
    val str: String,
    var value: Any? = null,
    var error: FileParserException? = null
)

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
            if (parser != null) {
                parser(str)
            } else {
                defaultTypeParser(str)
            }
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
