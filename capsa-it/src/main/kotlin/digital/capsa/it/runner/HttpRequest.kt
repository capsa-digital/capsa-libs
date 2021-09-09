package digital.capsa.it.runner

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.MapSerializer
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
data class HttpRequest(

    val method: HttpMethod,

    val schema: String? = null,

    val host: String? = null,

    val port: Int = -1,

    val basePath: String? = null,

    val path: String? = null,

    var proxyHost: String? = null,

    var proxyPort: String? = null,

    val queryParams: String? = null,

    @JsonSerialize(keyUsing = MapSerializer::class)
    val headers: Map<String, String> = mapOf(),

    val body: JsonNode? = null,

    val connectTimeout: Int = 10000,

    val readTimeout: Int = 10000
)
