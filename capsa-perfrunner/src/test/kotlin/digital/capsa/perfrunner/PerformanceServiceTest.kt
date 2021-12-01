package digital.capsa.perfrunner

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import digital.capsa.it.gherkin.given
import digital.capsa.perfrunner.domain.ExecutionGroup
import digital.capsa.perfrunner.domain.HttpRequest
import digital.capsa.perfrunner.domain.Plan
import digital.capsa.perfrunner.domain.URL
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(
    classes = [PerfRunnerTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("unit")
internal class PerformanceServiceTest {

    @LocalServerPort
    lateinit var port: String

    @Test
    fun `executePlan - Happy path - GET`() {
        given {
            Plan(
                "PlanName",
                ExecutionGroup(
                    "GroupName",
                    HttpRequest(
                        URL("http", "localhost", port.toInt(), "/testGet", ""),
                        HttpRequest.Method.GET,
                        null,
                        ""
                    ),
                    "3",
                    "0",
                    "0",
                    "1",
                    "200"
                )
            )
        }.on { plan ->
            PerformanceService().executePlan().apply(plan)
        }.then { report ->
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
            println(report.summary)
        }
    }

    @Test
    fun `executePlan - Happy path - POST`() {
        given {
            Plan(
                "PlanName",
                ExecutionGroup(
                    "GroupName",
                    HttpRequest(
                        URL("http", "localhost", port.toInt(), "/testPost", ""),
                        HttpRequest.Method.POST,
                        mapOf("my-header-value" to "Hello header"),
                        "Hello body"
                    ),
                    "1",
                    "0",
                    "0",
                    "1",
                    "200"
                )
            )
        }.on { plan ->
            PerformanceService().executePlan().apply(plan)
        }.then { report ->
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
            println(report.summary)
        }
    }

    @Test
    fun `executePlan - negative - two failures`() {
        given {
            Plan(
                "PlanName",
                ExecutionGroup(
                    "GroupName",
                    HttpRequest(
                        URL("http", "localhost", port.toInt(), "/testGetWithErrors", ""),
                        HttpRequest.Method.GET,
                        null,
                        ""
                    ),
                    "1",
                    "0",
                    "0",
                    "1",
                    "200"
                )
            )
        }.on { plan ->
            PerformanceService().executePlan().apply(plan)
        }.then { report ->
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
            assertThat(report.totalErrorCount).isEqualTo(2)
            println(report.summary)
        }
    }
}