package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate
import java.util.*

class Customer(
    var name: String? = null,
    var accountId: Locale? = null,
    var accountStatus: AccountStatus? = null
) : AbstractAggregate() {

    fun order(init: Order.() -> Unit) = initAggregate(Order(), init)

    override fun onCreate() {
    }

    override fun construct() {
    }

}

fun customer(init: Customer.() -> Unit): Customer {
    return Customer().apply {
        construct()
        init()
    }
}

enum class AccountStatus {
    Active, Disabled
}