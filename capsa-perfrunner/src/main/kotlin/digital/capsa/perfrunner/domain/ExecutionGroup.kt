package digital.capsa.perfrunner.domain

data class ExecutionGroup(
    val name: String,
    val request: HttpRequest,
    val targetConcurrency: String,
    val rampUpTime: String,
    val rampUpStepCount: String,
    val holdTargetRateTime: String,
    val assertionResponseCode: String
)
