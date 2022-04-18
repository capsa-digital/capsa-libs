package digital.capsa.it.httprequest.schema

data class HttpMultipartResponse(
    val numberOfFilesReceived: Int,
    val numberOfFilesSent: Int
)