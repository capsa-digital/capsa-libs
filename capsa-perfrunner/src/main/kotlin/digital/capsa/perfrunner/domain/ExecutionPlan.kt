package digital.capsa.perfrunner.domain

class ExecutionPlan(val name: String) {

    private val groups: MutableSet<ExecutionGroup>

    val executionGroup: Set<ExecutionGroup>
        get() = groups

    init {
        groups = HashSet()
    }

    fun addExecutionGroup(group: ExecutionGroup) {
        groups.add(group)
    }
}
