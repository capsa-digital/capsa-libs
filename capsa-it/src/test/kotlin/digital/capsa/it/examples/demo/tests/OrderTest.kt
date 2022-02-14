package digital.capsa.it.examples.demo.tests

import assertk.assertThat
import assertk.assertions.isEqualTo
import digital.capsa.it.aggregate.Key
import digital.capsa.it.examples.demo.impl.*
import digital.capsa.it.gherkin.given
import digital.capsa.it.json.isJsonWhere
import digital.capsa.it.runner.TabularSource
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@EnableAutoConfiguration
@SpringBootTest(classes = [OrderTestConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderTest {

    companion object OrderTestContext : ShopTestContext()

    @BeforeAll
    fun `prepare test data`() {
        shop = shop {
            item {
                itemNumber = "123"; amount = 20
            }
            item {
                itemNumber = "234"; amount = 30
            }
            customer {
                -"Customer-1"
                order {
                    itemNumber = "123"; amount = 1
                }
                order {
                    itemNumber = "234"; amount = 2
                }
            }
        }.apply { create(OrderTestContext) }
    }

    @TabularSource(
        """
        | 123 | 1 |
        | 124 | 5 |
        """
    )
    @ParameterizedTest
    fun `submit order test`(itemNum: String, quantity: Int) {
        given {
            `customer created`(
                context = OrderTestContext,
                customer = customer {
                    -"Customer-2"
                }
            )
        }.on {
            `order submitted`(
                context = OrderTestContext,
                customerKey = Key("Customer-2"),
                order = order {
                    itemNumber = itemNum
                    amount = quantity
                }
            )
        }.then {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            assertThat(body).isJsonWhere(
                ValidationRule("$.itemNumber", OpType.equal, itemNum)
            )
        }
    }
}
