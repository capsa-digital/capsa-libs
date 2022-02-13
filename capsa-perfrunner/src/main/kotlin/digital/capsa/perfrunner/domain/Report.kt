package digital.capsa.perfrunner.domain

data class Report(
    val summary: String,
    val totalCallCount: Long,
    val totalErrorCount: Long,
    val throughput: Double,
    val averageResponseTime: Double,
    val maxResponseTime: Long
)
