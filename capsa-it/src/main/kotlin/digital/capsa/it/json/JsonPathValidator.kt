package digital.capsa.it.json

import assertk.assertThat
import assertk.assertions.matches
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import net.minidev.json.JSONArray
import java.util.regex.Pattern
import kotlin.test.assertEquals

object JsonPathValidator {

    fun assertJson(json: String, rules: List<ValidationRule>) {
        val document = Configuration.defaultConfiguration().jsonProvider().parse(json)

        for (rule in rules) {
            val valueList: List<Any?> = if (rule.value is List<*>) {
                rule.value
            } else {
                listOf(rule.value)
            }
            var jsonPath: Any?
            try {
                jsonPath = JsonPath.read(document, rule.path)
            } catch (e: Exception) {
                throw Error("Path not found, document: $document, path: ${rule.path}", e)
            }
            if (jsonPath == null) {
                assertEquals(
                    valueList[0], null, "json path ${rule.path} validation failed, document: $document"
                )
            } else
                when (jsonPath) {
                    is JSONArray -> {
                        if (rule.op != OpType.equal) {
                            throw Error("'${rule.op}' op is not supported for JSONArray result. Use 'equal' op")
                        }
                        if (jsonPath.size == 0) {
                            assertEquals(
                                valueList.size,
                                0,
                                "json path ${rule.path} validation failed, document: $document"
                            )
                        } else {
                            assertEquals(
                                valueList.toSet(),
                                jsonPath.toSet(),
                                "json path ${rule.path} validation failed, document: $document"
                            )
                        }
                    }
                    is Number -> {
                        if (rule.op != OpType.equal) {
                            throw Error("${rule.op} op is not supported for Number result. Use 'equal' op")
                        }
                        assertEquals(
                            valueList[0], jsonPath, "json path ${rule.path} validation failed, document: $document"
                        )
                    }
                    is Boolean -> {
                        if (rule.op != OpType.equal) {
                            throw Error("${rule.op} op is not supported for Boolean result. Use 'equal' op")
                        }
                        assertEquals(
                            valueList[0], jsonPath, "json path ${rule.path} validation failed, document: $document"
                        )
                    }
                    is String ->
                        when (rule.op) {
                            OpType.regex ->
                                assertThat(
                                    jsonPath,
                                    "json path ${rule.path} validation failed, document: $document"
                                ).matches(Regex(valueList[0].toString(), RegexOption.DOT_MATCHES_ALL))
                            OpType.equal ->
                                assertEquals(
                                    valueList[0],
                                    jsonPath,
                                    "json path ${rule.path} validation failed, document: $document"
                                )
                            OpType.like ->
                                assertThat(
                                    jsonPath,
                                    "json path ${rule.path} validation failed, document: $document"
                                ).matches(Regex(".*${valueList[0]}.*", RegexOption.DOT_MATCHES_ALL))
                        }
                    else -> throw Error("Expected JSONArray or Number or String, received ${jsonPath.let { it::class }}")
                }
        }
    }
}

fun <T> assertk.Assert<T>.isJsonWhere(vararg validations: ValidationRule) = given { actual ->
    JsonPathValidator.assertJson(actual.toString(), validations.asList())
}
