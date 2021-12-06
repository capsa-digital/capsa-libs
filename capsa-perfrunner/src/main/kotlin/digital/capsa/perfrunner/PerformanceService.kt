package digital.capsa.perfrunner

import digital.capsa.perfrunner.domain.Plan
import digital.capsa.perfrunner.domain.Report
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class PerformanceService {

    @Bean
    fun executePlan(): (Plan) -> Report = { plan ->
        val jMeterSummariser: JMeterSummariser = JMeterRunner.execute(plan)
        Report(
            jMeterSummariser.getSummary(),
            jMeterSummariser.getNumSamples(),
            jMeterSummariser.getErrorCount()
        )
    }
}
