package digital.capsa.perfrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
open class PerfRunnerTestApp

fun main(args: Array<String>) {
    runApplication<PerfRunnerTestApp>(*args)
}

@RestController
class MainController {

    var callCount: Long = 0L

    @GetMapping("/test")
    fun getRequest(): String {
        callCount++
        return "Hello world"
    }

    @GetMapping("/getCallCount")
    fun getCallCount(): String {
        val returnValue = callCount.toString()
        callCount = 0
        return returnValue
    }
}