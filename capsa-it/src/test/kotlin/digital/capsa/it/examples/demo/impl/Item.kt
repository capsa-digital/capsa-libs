package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate

class Item(
    var itemNumber: Int? = null,
    var amount: Int? = null
) : AbstractAggregate() {

    override fun onCreate() {
    }

    override fun construct() {
    }

}

fun item(init: Item.() -> Unit): Item {
    return Item().apply {
        construct()
        init()
    }
}