package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate

class Item(
    var itemNumber: String? = null,
    var amount: Int? = null,
    var title: String? = null
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