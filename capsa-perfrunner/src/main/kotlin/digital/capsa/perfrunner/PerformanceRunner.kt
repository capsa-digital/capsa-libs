package digital.capsa.perfrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PerformanceRunner

fun main(vararg args: String) {
    runApplication<PerformanceRunner>(*args)
}