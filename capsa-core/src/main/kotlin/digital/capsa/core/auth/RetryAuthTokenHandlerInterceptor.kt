package digital.capsa.core.auth

import com.auth0.jwt.JWT
import digital.capsa.core.exceptions.AuthTokenException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class RetryAuthTokenHandlerInterceptor : ClientHttpRequestInterceptor {

    @Autowired
    private lateinit var clientCredentials: ClientCredentials

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        var response: ClientHttpResponse = execution.execute(request, body)
        if (HttpStatus.UNAUTHORIZED === response.statusCode) {
            val scope: String = getClaim(request.headers["Authorization"], "scope")
            val accessToken: String = clientCredentials.getAuthToken(scope, true)
            if (accessToken.isNotBlank()) {
                request.headers["Authorization"] = accessToken
                response = execution.execute(request, body)
            }
        }
        return response
    }

    private fun getClaim(headerAuthorization: MutableList<String>?, claimName: String): String {
        try {
            // Get the token, remove the Bearer word
            val token: String = headerAuthorization!!.first().split(" ")[1]
            val jwt = JWT.decode(token)
            return jwt.getClaim(claimName).asList(String::class.java).toList().map{it.trim()}.filter { it.isNotEmpty() }.distinct().first()
        } catch (e: Exception){
            throw AuthTokenException("Fail to extract $claimName from jwt token!", e)
        }
    }
}