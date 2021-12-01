package digital.capsa.perfrunner.domain

@Suppress("UnsafeCallOnNullableType")
data class HttpRequest(val url: URL, val method: Method, val headers: Map<String, String>?, val body:String) {

    enum class Method {
        GET, PUT, POST
    }

}
