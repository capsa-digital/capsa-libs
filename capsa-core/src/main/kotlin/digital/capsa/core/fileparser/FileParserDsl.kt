package digital.capsa.core.fileparser

import java.io.BufferedReader
import kotlin.math.min

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
        parse: RecordParser.() -> Any
    ) {
        if (lineCount < records.size) {
            processLine(lineCount++, length, parse)
        }
    }

    fun line(
        length: Int? = null,
        parse: RecordParser.() -> Any
    ) {
        while (lineCount < records.size) {
            processLine(lineCount++, length, parse)
        }
    }

    private fun processLine(lineIndex: Int, length: Int?, parse: RecordParser.() -> Any) {
        try {
            length?.let {
                if (records[lineIndex].str.length != it) {
                    records[lineIndex].issues.add(FileParserError("Line length should be $length but was ${records[lineIndex].str.length}"))
                }
            }
            val recordParser = RecordParser(records[lineIndex].str, lineIndex)
            records[lineIndex].value = recordParser.parse()
            records[lineIndex].issues.addAll(recordParser.issues)
        } catch (e: Exception) {
            records[lineIndex].issues.add(FileParserError(e.message ?: "Unknown parser error"))
        }
    }

    fun getRecords(): List<Record> {
        return records.toList()
    }
}

data class Record(
    val str: String,
    var value: Any? = null,
    var issues: MutableList<FileParserIssue> = mutableListOf()
)

class RecordParser(
    val line: String,
    val index: Int
) {
    var issues: MutableList<FileParserIssue> = mutableListOf()

    inline fun <reified R> optionalField(
        from: Int,
        toExclusive: Int,
        name: String? = null,
        default: () -> R? = { null },
        ignoreError: Boolean = false,
        noinline parser: ((String) -> R?)? = null
    ): R? {
        val str = readField(from, toExclusive)
        return if (str.isNotBlank()) {
            try {
                if (parser != null) {
                    parser(str)
                } else {
                    defaultTypeParser(str)
                }
            } catch (e: Exception) {
                if (ignoreError) {
                    issues.add(FileParserWarning(e.message ?: "Unknown parser warning"))
                    null
                } else {
                    throw e
                }
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

    fun readField(
        from: Int,
        toExclusive: Int
    ): String {
        return if (line.length > from) line.substring(from, min(toExclusive, line.length)).trimEnd() else ""
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

data class FileParserWarning(override var message: String = "Unknown parser warning") : FileParserIssue(message)

data class FileParserError(override var message: String = "Unknown parser error") : FileParserIssue(message)

abstract class FileParserIssue(open var message: String)

open class FileParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)