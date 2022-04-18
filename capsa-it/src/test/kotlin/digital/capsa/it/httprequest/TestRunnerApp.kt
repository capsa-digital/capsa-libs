package digital.capsa.it.httprequest

import digital.capsa.it.Tuple2
import digital.capsa.it.httprequest.schema.HttpMultipartResponse
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableAutoConfiguration
open class TestRunnerApp

@PostConstruct
fun main(args: Array<String>) {
    runApplication<TestRunnerApp>(*args)
}

@RestController
class MainController {
    @PostMapping("/testPostMultipartFormDataRequest")
    fun postMultipartFormDataRequest(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestPart("object") tuple: Tuple2<String, Int>
    ): ResponseEntity<HttpMultipartResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(
            HttpMultipartResponse(
                numberOfFilesSent = tuple._2,
                numberOfFilesReceived = files.size
            )
        )
    }
}