package digital.capsa.core.aggregate

abstract class Aggregate<T : AggregateId> {

    abstract var id: T

}
