package digital.capsa.core.validation

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.ValidationReport
import com.github.tomakehurst.wiremock.common.Urls
import com.github.tomakehurst.wiremock.http.LoggedResponse
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.util.ResolverFully
import java.net.URI

class SwaggerValidator {

    private val validator: HashMap<String, OpenApiInteractionValidator> = hashMapOf()

    fun validateEvents(events: List<ServeEvent>) {
        events.forEach {
            validateEvent(it)
        }
    }

    fun validateEvent(event: ServeEvent) {
        val swaggerFile: String? = event.stubMapping.metadata?.let { it["swaggerFile"].toString() }
        val mockIdentifier: String? = event.stubMapping.metadata?.let { it["mockIdentifier"].toString() }
        var report = ValidationReport.empty()

        if (swaggerFile != null && mockIdentifier != null && SwaggerValidator::class.java.getResource(swaggerFile) != null) {
            if (!validator.contains(swaggerFile)) {
                validator[swaggerFile] = OpenApiInteractionValidator
                    .createFor(
                        OpenAPIV3Parser()
                            .read(swaggerFile)
                            .also { openAPI ->
                                ResolverFully().resolveFully(openAPI)
                            }
                    )
                    .build()
            }

            report =
                report.merge(
                    validator[swaggerFile]!!.validate(
                        convertRequest(event.request, mockIdentifier),
                        convertResponse(event.response)
                    )
                )

            val errors = report.messages.filter { it.level.name == "ERROR" }

            if (errors.isNotEmpty()) {
                throw Error("Schema validation fail because we encounter errors: $errors\n" +
                    "for url: ${event.request.url}")
            }
        } else {
            println("The swagger file $swaggerFile, from resources folder, doesn't exist for url = ${event.request.url}")
        }
    }

    private fun convertRequest(loggedRequest: LoggedRequest, mockIdentifier: String): Request {
        // Remove the wiremock identifier
        val uri = URI.create(loggedRequest.url.removePrefix(mockIdentifier))

        return SimpleRequest.Builder(loggedRequest.method.name, uri.path)
            .withBody(loggedRequest.body)
            .also { builder ->
                loggedRequest.headers.all().forEach { header ->
                    builder.withHeader(header.key(), header.values())
                }
                Urls.splitQuery(uri).forEach { (key, value) ->
                    builder.withQueryParam(key, value.values())
                }
            }.build()
    }

    private fun convertResponse(loggedResponse: LoggedResponse): Response {
        return SimpleResponse.Builder(loggedResponse.status)
            .withBody(loggedResponse.body)
            .also { builder ->
                loggedResponse.headers.all().forEach { header ->
                    builder.withHeader(header.key(), header.values())
                }
            }.build()
    }
}