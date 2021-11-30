package digital.capsa.perfrunner

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import digital.capsa.it.gherkin.given
import digital.capsa.perfrunner.domain.ExecutionGroup
import digital.capsa.perfrunner.domain.HttpRequest
import digital.capsa.perfrunner.domain.Plan
import digital.capsa.perfrunner.domain.URL
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Disabled //TODO Only runs locally due to PerfRunnerTestApp that needs to be running
@Tag("unit")
internal class PerfServiceTest {

    @Test
    fun `executePlan Happy path`() {
        given {
            PerfService()
        }.on { perfService ->
            perfService.executePlan().apply(
                Plan(
                    "test", ExecutionGroup(
                        "GroupName",
                        HttpRequest(URL("http", "localhost", 8081, "/test", ""), HttpRequest.Method.GET, null, ""),
                        "1",
                        "0",
                        "0",
                        "1",
                        "200"
                    )
                )
            )
        }.then { report ->
            assertThat(report.totalCallCount).isGreaterThan(100)
            assertThat(
                java.net.URL("http://localhost:8081/getCallCount").readText().toLong()
            ).isEqualTo(report.totalCallCount)
        }
    }
}