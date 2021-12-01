package digital.capsa.perfrunner

import digital.capsa.perfrunner.domain.ExecutionPlan
import digital.capsa.perfrunner.domain.Report
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Function

@Service
class PerformanceService {

    @Bean
    fun executePlan(): Function<ExecutionPlan, Report> {
        return Function { execPlan ->
            val jMeterSummariser: JMeterSummariser = JMeterRunner.exec(execPlan)
            return@Function Report(
                jMeterSummariser.getSummary(),
                jMeterSummariser.getNumSamples(),
                jMeterSummariser.getErrorCount()
            )
        }
    }
}
