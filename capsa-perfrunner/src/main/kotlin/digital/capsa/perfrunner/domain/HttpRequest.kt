package digital.capsa.perfrunner.domain

@Suppress("UnsafeCallOnNullableType")
data class HttpRequest(var url: URL, var method: Method, var headers: Map<String, String>?, var body:String) {

    enum class Method {
        GET, PUT, POST
    }

}
