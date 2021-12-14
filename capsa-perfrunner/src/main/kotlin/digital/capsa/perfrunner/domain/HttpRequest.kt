package digital.capsa.perfrunner.domain

data class HttpRequest(
    val url: URL,
    val method: Method,
    val headers: Map<String, String>? = null,
    val body: String? = null
) {

    enum class Method {
        GET, PUT, POST
    }
}
