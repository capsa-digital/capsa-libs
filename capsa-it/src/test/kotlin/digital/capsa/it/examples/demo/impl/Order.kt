package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate

class Order(
    var itemNumber: Int? = null,
    var amount: Int? = null
) : AbstractAggregate() {

    override fun onCreate() {
    }

    override fun construct() {
    }

}

fun order(init: Order.() -> Unit): Order {
    return Order().apply {
        construct()
        init()
    }
}