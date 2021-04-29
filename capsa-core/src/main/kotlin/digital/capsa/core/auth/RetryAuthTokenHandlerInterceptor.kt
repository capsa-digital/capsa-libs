package digital.capsa.core.auth

import digital.capsa.core.auth.ClientCredentials
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
            val accessToken: String = clientCredentials.getAuthToken(true)
            if (accessToken.isNotBlank()) {
                request.headers["Authorization"] = accessToken
                response = execution.execute(request, body)
            }
        }
        return response
    }
}