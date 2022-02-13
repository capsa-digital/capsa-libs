package digital.capsa.perfrunner.domain

data class Plan(
    val name: String,
    val groups: Set<ExecutionGroup>
)
