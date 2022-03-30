package digital.capsa.it.runner

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import digital.capsa.it.json.JsonPathModifier
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class HttpRequestBuilder(private val objectMapper: ObjectMapper, private val requestFile: String) {

    private var transformations: Map<String, Any?> = mutableMapOf()

    private var token: String? = null

    fun withTransformation(vararg transformations: Pair<String, Any?>): HttpRequestBuilder {
        this.transformations += transformations.toMap()
        return this
    }

    fun withAuthenticationToken(token: String?): HttpRequestBuilder {
        this.token = token
        return this
    }

    fun send(block: (ResponseEntity<String>.() -> Unit)? = null): ResponseEntity<String> {
        val requestJson = javaClass.getResourceAsStream(requestFile)
            .let(::InputStreamReader)
            .let(::BufferedReader)
            .lines()
            .collect(Collectors.joining())

        val transformRequestJson: String = JsonPathModifier.modifyJson(requestJson, transformations)
        val httpRequest = objectMapper.readValue(transformRequestJson, HttpRequest::class.java)

        val restTemplate = RestTemplate(
            getClientHttpRequestFactory(
                httpRequest.connectTimeout,
                httpRequest.readTimeout,
                httpRequest.proxyHost,
                httpRequest.proxyPort
            )
        )
        restTemplate.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return false
            }

            override fun handleError(response: ClientHttpResponse) {}
        }

        val headers = HttpHeaders()

        token?.let { token ->
            headers.setBearerAuth(token)
        }

        httpRequest.headers.forEach { (key, value) -> headers[key] = value }

        val body = httpRequest.body?.takeIf { it is TextNode }?.textValue() ?: httpRequest.body.toString()
        val requestEntity = HttpEntity(body, headers)

        val response = restTemplate.exchange(
            UriComponentsBuilder.newInstance()
                .scheme(httpRequest.schema)
                .host(httpRequest.host)
                .port(httpRequest.port)
                .path((httpRequest.basePath?.let { "${httpRequest.basePath}" } ?: "") + httpRequest.path)
                .query(httpRequest.queryParams)
                .build(true)
                .toUri(),
            httpRequest.method, requestEntity, String::class.java
        )

        if (block != null) {
            response.block()
        }

        return response
    }

    private fun getClientHttpRequestFactory(
        connectTimeout: Int,
        readTimeout: Int,
        proxyHost: String?,
        proxyPort: String?
    ):
            HttpComponentsClientHttpRequestFactory {
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory(
            HttpClientBuilder.create()
                .setProxy(proxyHost?.let { HttpHost(it, proxyPort!!.toInt(), "http") })
                .build()
        )
        clientHttpRequestFactory.setConnectTimeout(connectTimeout)
        clientHttpRequestFactory.setReadTimeout(readTimeout)
        clientHttpRequestFactory.httpClient = HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLSocketFactory(
                SSLConnectionSocketFactory(
                    SSLContexts.custom()
                        .loadTrustMaterial(null) { _, _ -> true }
                        .build(), null, null,
                    NoopHostnameVerifier()
                )
            ).build()
        return clientHttpRequestFactory
    }
}
