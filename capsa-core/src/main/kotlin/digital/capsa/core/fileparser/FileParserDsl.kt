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
            val recordParser = RecordParser(records[lineIndex].str)
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
    private val line: String
) {
    var issues: MutableList<FileParserIssue> = mutableListOf()

    inline fun <reified R> optionalField(
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

    inline fun <reified R> nullableField(
        from: Int,
        toExclusive: Int,
        name: String? = null,
        default: () -> R? = { null },
        noinline parser: ((String) -> R)? = null
    ): R? {
        val str = readField(from, toExclusive)
        return if (str.isNotBlank()) {
            if (parser != null) {
                try {
                    parser(str)
                } catch (e:RuntimeException) {
                    issues.add(FileParserWarning(e.message ?: "Unknown parser warning"))
                    null
                }
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

open class FileParserWarning(message: String = "Unknown parser warning") : FileParserIssue(message)

open class FileParserError(message: String = "Unknown parser error") : FileParserIssue(message)

abstract class FileParserIssue(var message: String)

open class FileParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)