package digital.capsa.perfrunner.domain

data class URL(
    val scheme: String,
    val host: String,
    val port: Int?,
    val path: String,
    val query: String
) {
}
