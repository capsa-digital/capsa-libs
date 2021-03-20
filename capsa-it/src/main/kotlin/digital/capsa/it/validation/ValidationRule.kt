package digital.capsa.it.validation

data class ValidationRule(val path: String, val op: OpType, val value: Any?)