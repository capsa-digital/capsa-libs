package digital.capsa.core.exceptions

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    val code: String? = null,
    val reason: String? = null,
    val message: String? = null,
    val status: String,
    val referenceError: String? = null,

    @get:JsonProperty("@baseType")
    val baseType: String? = null,

    @get:JsonProperty("@schemaLocation")
    val schemaLocation: String? = null,

    @get:JsonProperty("@type")
    val type: String? = null
) {
    companion object {
        tailrec fun getCause(t: Throwable): Throwable {
            return if (t.cause != null) getCause(t.cause as Throwable) else t
        }
    }
}
