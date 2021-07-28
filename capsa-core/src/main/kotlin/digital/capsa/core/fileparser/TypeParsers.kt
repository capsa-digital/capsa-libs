package digital.capsa.core.fileparser

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

inline fun <reified T> defaultTypeParser(parse: String): T = parse.parseToType(T::class)

@OptIn(ExperimentalUnsignedTypes::class)
fun <T> String.parseToType(type: KClass<*>): T {
    if (type.isSubclassOf(Enum::class)) return parseToEnum(type) as T

    return when (type) {
        String::class -> parseToString()
        Int::class -> parseToInt()
        UInt::class -> parseToUInt()
        Double::class -> parseToDouble()
        Long::class -> parseToLong()
        ULong::class -> parseToULong()
        Char::class -> parseToChar()
        Boolean::class -> parseToBoolean()
        LocalDate::class -> parseToLocalDate()
        LocalDateTime::class -> parseToLocalDateTime()
        LocalTime::class -> parseToLocalTime()
        BigDecimal::class -> parseToBigDecimal()
        else -> throw NoParserForClass(type)
    } as T
}

class NoParserForClass(
        klass: KClass<*>
) : RuntimeException("There are no default parsers for class $klass. Please provide a custom parser")

private fun String.parseToString() = this

private fun String.parseToInt() = this.toInt()

@OptIn(ExperimentalUnsignedTypes::class)
private fun String.parseToUInt() = this.toUInt()

private fun String.parseToDouble() = this.toDouble()

private fun String.parseToLong() = this.toLong()

@OptIn(ExperimentalUnsignedTypes::class)
private fun String.parseToULong() = this.toULong()

private fun String.parseToChar() = this.single()

private fun String.parseToBoolean() = this.toBoolean()

private fun String.parseToLocalDate() = LocalDate.parse(this)

private fun String.parseToLocalDateTime() = LocalDateTime.parse(this)

private fun String.parseToLocalTime() = LocalTime.parse(this)

private fun String.parseToBigDecimal() = this.toBigDecimal()

private fun String.parseToEnum(enumClass: KClass<*>): Enum<*> {
    val enumConstants = enumClass.java.enumConstants as Array<Enum<*>>
    return enumConstants.first { it.name == this }
}

inline fun <reified T : Number> String.parseToDecimal(scale: Int): T {
    val builder = StringBuilder(this)
    builder.insert(this.length - scale, '.')
    val string = builder.toString()

    return when (T::class) {
        Double::class -> string.toDouble()
        BigDecimal::class -> string.toBigDecimal()
        else -> throw NoDecimalParserForClass(T::class)
    } as T
}

class NoDecimalParserForClass(
        klass: KClass<*>
) : RuntimeException("There are no default decimal parsers for class $klass. Please use a custom parser instead.")