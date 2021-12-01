package digital.capsa.perfrunner.domain

data class ExecutionPlan(
    val name: String,
    val groups: Set<ExecutionGroup>
)
