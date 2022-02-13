package digital.capsa.it.examples.demo.tests

import assertk.assertThat
import assertk.assertions.isEqualTo
import digital.capsa.it.aggregate.Key
import digital.capsa.it.examples.demo.impl.*
import digital.capsa.it.gherkin.given
import digital.capsa.it.json.isJsonWhere
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@EnableAutoConfiguration
@SpringBootTest(classes = [OrderTestConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderTest {

    private companion object OrderTestContext : ShopTestContext()

    @BeforeAll
    fun `prepare test data`() {
        shop = shop {
            item {
                itemNumber = 123; amount = 20
            }
            item {
                itemNumber = 234; amount = 30
            }
            customer {
                +Key("Customer-1")
                order {
                    itemNumber = 123; amount = 1
                }
                order {
                    itemNumber = 234; amount = 2
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
                order = order {
                    itemNumber = 123
                    amount = 1
                }
            )
        }.then {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            assertThat(body).isJsonWhere(
                ValidationRule("$.itemNumber", OpType.equal, "123")
            )
        }
    }
}
