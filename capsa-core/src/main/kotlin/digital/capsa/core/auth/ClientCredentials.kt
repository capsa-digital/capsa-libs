package digital.capsa.core.auth

import digital.capsa.core.exceptions.AuthTokenException
import digital.capsa.core.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.Instant
import javax.xml.bind.DatatypeConverter

class ClientCredentials {

    companion object {
        var authToken: AuthToken? = null
    }

    @Value("\${auth-token-service.host}")
    private lateinit var authTokenServiceHost: String

    @Value("\${auth-token-service.port}")
    private lateinit var authTokenServicePort: String

    @Value("\${auth-token-service.schema}")
    private lateinit var authTokenServiceSchema: String

    @Value("\${auth-token-service.basePath}")
    private lateinit var authTokenServiceBasePath: String

    @Value("\${auth-token-service.client-id}")
    private lateinit var authTokenServiceClientId: String

    @Value("\${auth-token-service.client-secret}")
    private lateinit var authTokenServiceClientSecret: String

    @Value("\${auth-token-service.scope}")
    private lateinit var authTokenServiceScope: String

    @Value("\${auth-token-service.timeout-buffer:20}")
    private lateinit var authTokenServiceTimeoutBuffer: String

    @Suppress("MaxLineLength")
    private val authTokenServiceBaseUri: String by lazy {
        "$authTokenServiceSchema://$authTokenServiceHost:$authTokenServicePort/$authTokenServiceBasePath/st/token"
    }

    fun getAuthToken(forceRefresh: Boolean = false): String {
        refreshAuthToken(forceRefresh)
        return "Bearer ${authToken.also { it?.usageCounter?.incrementAndGet() }?.token}"
    }

    @Synchronized
    fun refreshAuthToken(forceRefresh: Boolean) {
        if (authToken == null || forceRefresh || authToken?.expiryDate?.isBefore(Instant.now()) != true) {
            var exception: RestClientException? = null
            // Retry 3 times
            for (attempt in 1..3) {
                try {
                    authToken = retrieveAuthToken()
                    break
                } catch (e: RestClientException) {
                    exception = e
                    logger.warn("Failed refreshAuthToken attempt $attempt", e)
                }
            }
            exception?.let { throw it }
        }
    }

    private fun retrieveAuthToken(): AuthToken {
        val requestHeaders = HttpHeaders()
        requestHeaders.add(
            "Authorization", "Basic " +
                    DatatypeConverter.printBase64Binary(
                        "$authTokenServiceClientId:$authTokenServiceClientSecret".toByteArray()
                    )
        )
        requestHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        requestHeaders.accept = listOf(MediaType.APPLICATION_JSON)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("grant_type", "client_credentials")
        params.add("scope", authTokenServiceScope)

        return try {
            RestTemplate().exchange(
                authTokenServiceBaseUri,
                HttpMethod.POST,
                HttpEntity(params, requestHeaders),
                AuthResponse::class.java
            ).body!!.transform()
        } catch (e: RestClientException) {
            throw AuthTokenException("Failed to retrieve auth token", e)
        }
    }

    private fun AuthResponse.transform(): AuthToken = AuthToken(
        token = access_token,
        tokenType = token_type,
        expiryDate = Instant.now().plusSeconds((expires_in - authTokenServiceTimeoutBuffer.toInt()).toLong())
    )
}
