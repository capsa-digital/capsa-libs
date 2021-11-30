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
internal class PerfServiceTest {

    @LocalServerPort
    lateinit var port: String

    @Test
    fun `executePlan - Happy path - GET`() {
        given {
            PerfService()
        }.on { perfService ->
            perfService.executePlan().apply(
                Plan(
                    "test", ExecutionGroup(
                        "GroupName",
                        HttpRequest(
                            URL("http", "localhost", port.toInt(), "/testGet", ""),
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
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(
                java.net.URL("http://localhost:$port/getCallCount").readText().toLong()
            ).isEqualTo(report.totalCallCount)
        }
    }

    @Test
    fun `executePlan - Happy path - POST`() {
        given {
            PerfService()
        }.on { perfService ->
            perfService.executePlan().apply(
                Plan(
                    "test", ExecutionGroup(
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
            )
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(
                java.net.URL("http://localhost:$port/getCallCount").readText().toLong()
            ).isEqualTo(report.totalCallCount)
        }
    }

    @Test
    fun `executePlan - negative - two failures`() {
        given {
            PerfService()
        }.on { perfService ->
            perfService.executePlan().apply(
                Plan(
                    "test", ExecutionGroup(
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
            )
        }.then { report ->
            println(report.summary)
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(
                java.net.URL("http://localhost:$port/getCallCount").readText().toLong()
            ).isEqualTo(report.totalCallCount)
            assertThat(report.totalErrorCount).isEqualTo(2)
        }
    }
}