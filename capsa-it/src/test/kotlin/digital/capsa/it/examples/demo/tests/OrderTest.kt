package digital.capsa.it.examples.demo.tests

import assertk.assertThat
import assertk.assertions.isEqualTo
import digital.capsa.it.TestContext
import digital.capsa.it.aggregate.Key
import digital.capsa.it.examples.demo.impl.*
import digital.capsa.it.gherkin.given
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@EnableAutoConfiguration
@SpringBootTest(classes = [OrderTestConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderTest {

    private companion object OrderTestContext : TestContext

    private lateinit var shop: Shop

    @BeforeAll
    fun `prepare test data`() {
        shop = shop {
            customer {
                +Key("Customer-1")
                order {
                }
                order {
                }
            }
        }.apply { create(OrderTestContext) }
    }

    @Test
    fun `submit order test`() {
        given {
            `customer created`(
                shop = shop,
                customer = customer {
                    +Key("Customer-2")
                }
            )
        }.on {
            `order submitted`(
                shop = shop,
                customerKey = Key("Customer-2"),
                order = order {}
            )
        }.then {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    private fun `customer created`(shop: Shop, customer: Customer): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.OK)
    }

    private fun `order submitted`(shop: Shop, customerKey: Key, order: Order): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.OK)
    }
}
