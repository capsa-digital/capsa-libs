package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate
import java.util.*

class Shop(
    var name: String? = null,
    var accountId: Locale? = null,
    var accountStatus: AccountStatus? = null
) : AbstractAggregate() {

    fun customer(init: Customer.() -> Unit) = initAggregate(Customer(), init)

    override fun onCreate() {
    }

    override fun construct() {
    }

}

fun shop(init: Shop.() -> Unit): Shop {
    return Shop().apply {
        construct()
        init()
    }
}
