package digital.capsa.it.examples.demo.impl

import digital.capsa.it.aggregate.AbstractAggregate
import digital.capsa.it.aggregate.Key
import digital.capsa.it.examples.demo.tests.ShopTestContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

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

fun `order submitted`(context: ShopTestContext, customerKey: Key, order: Order): ResponseEntity<String> {
    // Set-up Customer Mock
    // Set-up Order Mock
    return ResponseEntity(
    """{
            "itemNumber": "123"
        }""".trimIndent(), HttpStatus.OK
    )
}