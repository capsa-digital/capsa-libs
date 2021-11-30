package digital.capsa.perfrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@SpringBootApplication
open class PerfRunnerTestApp

fun main(args: Array<String>) {
    runApplication<PerfRunnerTestApp>(*args)
}

@RestController
class MainController {

    var callCount: Long = 0L

    @GetMapping("/testGet")
    fun getRequest(): String {
        callCount++
        return "Hello world"
    }

    @GetMapping("/testGetWithErrors")
    fun getRequestWithErrors(): ResponseEntity<String> {
        callCount++
        if (callCount == 3L || callCount == 7L) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity("Hello world", HttpStatus.OK)
    }

    @PostMapping("/testPost")
    fun postRequest(@RequestHeader("my-header-value") headerContent: String, @RequestBody body: String): String {
        if (headerContent == "Hello header" && body == "Hello body") {
            callCount++
        }
        return "Hello universe"
    }

    @GetMapping("/getCallCount")
    fun getCallCount(): String {
        val returnValue = callCount.toString()
        callCount = 0
        return returnValue
    }
}