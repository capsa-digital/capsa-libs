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
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

class ClientCredentials {

    companion object {
        /*
         * cache of AuthToken objects with scope for a key
         */
        var authTokenCache: MutableMap<String, AuthToken> = mutableMapOf()
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

    @Value("\${auth-token-service.timeout-buffer:20}")
    private lateinit var authTokenServiceTimeoutBuffer: String

    @Suppress("MaxLineLength")
    private val authTokenServiceBaseUri: String by lazy {
        "$authTokenServiceSchema://$authTokenServiceHost:$authTokenServicePort/$authTokenServiceBasePath/st/token"
    }

    fun getAuthToken(scope: String, forceRefresh: Boolean = false): String {
        refreshAuthToken(
            scope = scope,
            forceRefresh = forceRefresh
        )
        return "Bearer ${authTokenCache[scope].also { it?.usageCounter?.incrementAndGet() }?.token}"
    }

    @Synchronized
    private fun refreshAuthToken(scope: String, forceRefresh: Boolean) {
        if (authTokenCache[scope] == null || forceRefresh || authTokenCache[scope]?.expiryDate?.isBefore(ZonedDateTime.now())!!) {
            var exception: RestClientException? = null
            // Retry 3 times
            for (attempt in 1..3) {
                try {
                    authTokenCache[scope]?.let {
                        if (it.usageCounter.get() < 1) {
                            logger.warn("The usage of authToken for scope: $scope is ${authTokenCache[scope]!!.usageCounter.get()} times.")
                        }
                    }
                    authTokenCache[scope] = retrieveAuthToken(scope)
                    break
                } catch (e: RestClientException) {
                    exception = e
                    logger.warn("Failed refreshAuthToken attempt $attempt", e)
                }
            }
            exception?.let { throw it }
        }
    }

    private fun retrieveAuthToken(scope: String): AuthToken {
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
        params.add("scope", scope)

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
        expiryDate = ZonedDateTime.now().plusSeconds((expires_in - authTokenServiceTimeoutBuffer.toInt()).toLong())
    )
}
