package digital.capsa.core.auth

import digital.capsa.core.exceptions.AuthTokenException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.Instant
import javax.xml.bind.DatatypeConverter


@Component
class ClientCredentials {

    companion object {
        var authToken: AuthToken? = null
    }

    @Value("\${auth-token-service.endpoint-address:}")
    private lateinit var authTokenServiceEndpointAddress: String

    @Value("\${auth-token-service.client-id:}")
    private lateinit var authTokenServiceClientId: String

    @Value("\${auth-token-service.client-secret:}")
    private lateinit var authTokenServiceClientSecret: String

    @Value("\${auth-token-service.scope:}")
    private lateinit var authTokenServiceScope: String

    @Value("\${auth-token-service.timeout-buffer:20}")
    private lateinit var authTokenServiceTimeoutBuffer: String


    fun getAuthToken(forceRefresh: Boolean = false): String? {
        refreshAuthToken(forceRefresh)
        val token = authToken.also { it?.usageCounter?.incrementAndGet() }?.token
        return token?.let { "Bearer $it" }
    }

    @Synchronized
    fun refreshAuthToken(forceRefresh: Boolean) {
        if (authTokenServiceClientSecret.isNotEmpty())
            if (authToken == null || forceRefresh || authToken?.expiryDate?.isBefore(Instant.now()) != true) {
                authToken = retrieveAuthToken()
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
                authTokenServiceEndpointAddress,
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

