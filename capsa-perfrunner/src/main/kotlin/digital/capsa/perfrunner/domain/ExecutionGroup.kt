package digital.capsa.perfrunner.domain

data class ExecutionGroup(
    var name: String,
    var request: HttpRequest,
    var targetConcurrency: String,
    var rampUpTime: String,
    var rampUpStepCount: String,
    var holdTargetRateTime: String,
    var assertionResponseCode: String
)