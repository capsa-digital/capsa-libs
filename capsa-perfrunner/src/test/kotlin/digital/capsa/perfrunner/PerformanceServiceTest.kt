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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(
    classes = [PerformanceRunnerTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("unit")
internal class PerformanceServiceTest {

    @LocalServerPort
    lateinit var port: String

    @Autowired
    lateinit var performanceService: PerformanceService

    @Test
    fun `executePlan - positive - GET`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName1",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testGet"),
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
            )
        }.on { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
        }
    }

    @Test
    fun `executePlan - positive - GET with query params`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName1",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testGetWithParams?number=777&word=cat"),
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
            )
        }.on { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
        }
    }

    @Test
    fun `executePlan - positive - POST`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testPost"),
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
            )
        }.on { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
        }
    }

    @Test
    fun `executePlan - positive - POST with query params`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName1",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testPostWithParams?number=777&word=cat"),
                            HttpRequest.Method.POST,
                            null,
                            "Hello body!"
                        ),
                        "1",
                        "0",
                        "0",
                        "1",
                        "200"
                    )
                )
            )
        }.on { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
        }
    }

    @Test
    fun `executePlan - negative - two failures`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testGetWithErrors"),
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
            )
        }.on { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(report.totalCallCount)
                .isEqualTo(java.net.URL("http://localhost:$port/getCallCount").readText().toLong())
            assertThat(report.totalErrorCount).isEqualTo(2)
        }
    }

    @Test
    fun `executePlan - negative - GET with body`() {
        given {
            Plan(
                "PlanName",
                setOf(
                    ExecutionGroup(
                        "GroupName",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testGetWithErrors"),
                            HttpRequest.Method.GET,
                            null,
                            "Non-empty body"
                        ),
                        "1",
                        "0",
                        "0",
                        "1",
                        "200"
                    )
                )
            )
        }.onError { plan ->
            performanceService.executePlan().invoke(plan)
        }.then { error ->
            assertThat(error::class.simpleName).isEqualTo("PerfRunnerException")
            assertThat(error.message).isEqualTo("GET method does not support sending request body")
        }
    }
}
