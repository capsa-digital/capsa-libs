package digital.capsa.core.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import digital.capsa.core.exceptions.AuthTokenException
import digital.capsa.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@Configuration
class RetryAuthTokenHandlerInterceptor : ClientHttpRequestInterceptor {

    @Autowired
    private lateinit var clientCredentials: ClientCredentials

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        var response: ClientHttpResponse = execution.execute(request, body)
        if (HttpStatus.UNAUTHORIZED === response.statusCode) {
            val scope: String = getClaim(request.headers["Authorization"], "scope")
            val accessToken: String = clientCredentials.getAuthToken(scope, true)
            if (accessToken.isNotBlank()) {
                request.headers["Authorization"] = accessToken
                logger.warn("Retry call for ${request.uri}")
                response = execution.execute(request, body)
            }
        }
        return response
    }

    private fun getClaim(headerAuthorization: MutableList<String>?, claimName: String): String {
        try {
            // Get the token, remove the Bearer word
            val token: String = headerAuthorization?.first()?.split(" ")?.get(1)
                ?: throw AuthTokenException("Failed to extract Authorization token!", null)
            return JWT.decode(token).getClaim(claimName).asList(String::class.java).map { it.trim() }
                .filter { it.isNotEmpty() }.distinct().first()
        } catch (e: JWTDecodeException) {
            throw AuthTokenException("Failed to extract $claimName from jwt token!", e)
        }
    }
}