package digital.capsa.it.examples.demo.tests

import digital.capsa.it.TestContext
import digital.capsa.it.examples.demo.impl.Shop

abstract class ShopTestContext : TestContext {
    lateinit var shop: Shop
}