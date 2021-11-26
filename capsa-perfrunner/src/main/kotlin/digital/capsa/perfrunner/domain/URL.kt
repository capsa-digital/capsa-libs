package digital.capsa.perfrunner.domain

import java.net.URI
import java.net.URISyntaxException

data class URL(val scheme: String, val host: String, val port: Int?, val path: String, val query: String) {

    val raw: String
        @Throws(URISyntaxException::class)
        get() = URI(scheme, null, host, port ?: 0, path, query, null).toString()
}
