package digital.capsa.it.httprequest

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import digital.capsa.it.TestConfig
import digital.capsa.it.Tuple2
import digital.capsa.it.gherkin.given
import digital.capsa.it.json.isJsonWhere
import digital.capsa.it.runner.HttpMultipartFileEntity
import digital.capsa.it.runner.HttpRequestBuilder
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.random.Random

@SpringBootTest(
    classes = [TestRunnerApp::class, TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("unit")
internal class TestRunner {
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @LocalServerPort
    lateinit var port: String

    @Test
    fun `test multipart form data`() {
        val numberOfFiles = Random.Default.nextInt(20)

        given {
            mutableListOf<Pair<String, HttpEntity<Any>>>().apply {
                repeat(numberOfFiles) { index ->
                    add("files[]" to
                        HttpMultipartFileEntity("files", "firstFile$index.txt", Random.nextBytes(1e5.toInt())).httpEntity
                    )
                }
                add("object" to
                    HttpEntity<Any>(Tuple2("number_of_sent_files", numberOfFiles), HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })
                )
            }
        }.on { multiPartFormDataBody ->
            HttpRequestBuilder(objectMapper, "/requestFile.json")
                .withTransformation("port" to port)
                .withTransformation("basePath" to "/testPostMultipartFormDataRequest")
                .withMultiPartFormData(*multiPartFormDataBody.toTypedArray())
                .send()
        }.then { response ->
            assertThat(response.statusCode.value()).isEqualTo(200)
            assertThat(response.body).isJsonWhere(
                ValidationRule("numberOfFilesReceived", OpType.equal, numberOfFiles),
                ValidationRule("numberOfFilesSent", OpType.equal, numberOfFiles)
            )
        }
    }
}
