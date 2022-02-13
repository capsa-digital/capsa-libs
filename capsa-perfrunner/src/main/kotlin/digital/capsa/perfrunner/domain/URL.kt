package digital.capsa.perfrunner.domain

data class URL(
    val scheme: String,
    val host: String,
    val port: Int? = null,
    val path: String? = null
)
