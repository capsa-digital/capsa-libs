package digital.capsa.perfrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong

@SpringBootApplication
open class PerformanceRunnerTestApp

fun main(args: Array<String>) {
    runApplication<PerformanceRunnerTestApp>(*args)
}

@RestController
class MainController {

    private val callCount = AtomicLong(0)

    @GetMapping("/testGet")
    fun getRequest(): String {
        return "Count: ${callCount.getAndIncrement()}"
    }

    @GetMapping("/testGetWithParams")
    fun getParamRequest(
        @RequestParam("number") number: String,
        @RequestParam("word") word: String,
    ): String {
        return if (number == "777" && word == "cat") {
            "Count: ${callCount.getAndIncrement()}"
        } else {
            "Something's not right"
        }
    }

    @PostMapping("/testPostWithParams")
    fun postParamRequest(
        @RequestParam("number") number: String,
        @RequestParam("word") word: String,
        @RequestBody body: String
    ): String {
        return if (number == "777" && word == "cat" && body == "Hello body!") {
            "Count: ${callCount.getAndIncrement()}"
        } else {
            "Something's not right"
        }
    }

    @GetMapping("/testGetWithErrors")
    fun getRequestWithErrors(): ResponseEntity<String> {
        callCount.incrementAndGet()
        if (callCount.get() == 3L || callCount.get() == 7L) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity("Hello world", HttpStatus.OK)
    }

    @PostMapping("/testPost")
    fun postRequest(@RequestHeader("my-header-value") headerContent: String, @RequestBody body: String): String {
        if (headerContent == "Hello header" && body == "Hello body") {
            callCount.incrementAndGet()
        }
        return "Hello universe"
    }

    @GetMapping("/getCallCount")
    fun getCallCount(): String {
        val returnValue = callCount.toString()
        callCount.set(0)
        return returnValue
    }
}
