package digital.capsa.core.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(
    classes = [
        TestConfig::class,
        TokenService::class
    ],
    properties = [
        // configure ClientCredentials
        "auth-token-service.host=localhost",
        "auth-token-service.port=0", // the actual value is set from beforeAll()
        "auth-token-service.schema=http",
        "auth-token-service.basePath=auth",
        "auth-token-service.path=token",
        "auth-token-service.client-id=test",
        "auth-token-service.client-secret=test",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientCredentialsTest {

    @BeforeAll
    fun beforeAll(@LocalServerPort port: Int, @Autowired env: ConfigurableEnvironment) {
        // configure port
        env.propertySources.addFirst(
            MapPropertySource("TokenServicePort", mapOf("auth-token-service.port" to port))
        )
    }

    @BeforeEach
    fun beforeEach(@Autowired tokenService: TokenService) {
        tokenService.reset()
    }

    @Test
    fun testTokenHappyPath(
        @Autowired clientCredentials: ClientCredentials,
        @Autowired tokenService: TokenService
    ) {
        val token1 = clientCredentials.getAuthToken("test")
        assertThat(token1).isEqualTo("Bearer TestToken")
        assertThat(tokenService.getRequestCount()).isEqualTo(1)
        // 2nd response is taken from cache
        val token2 = clientCredentials.getAuthToken("test")
        assertThat(token2).isEqualTo(token1)
        // no additional server requests
        assertThat(tokenService.getRequestCount()).isEqualTo(1)
    }

    @Test
    fun testTokenRetry(
        @Autowired clientCredentials: ClientCredentials,
        @Autowired tokenService: TokenService
    ) {
        tokenService.setErrorResponse(true)
        assertThatExceptionOfType(HttpServerErrorException::class.java)
            .isThrownBy { clientCredentials.getAuthToken("test") }
        // ensure 3 attempts made
        assertThat(tokenService.getRequestCount()).isEqualTo(3)
    }
}

@Configuration
open class TestConfig {
    @Bean
    @Lazy // enable dynamic port configuration
    open fun tokenClient(): ClientCredentials {
        return ClientCredentials()
    }
}

@RestController // simulate token service
open class TokenService {
    private val requestCount = AtomicInteger(0)
    private var isErrorResponse = false

    @PostMapping("\${auth-token-service.basePath}/\${auth-token-service.path}")
    fun getToken(): AuthResponse {
        requestCount.incrementAndGet()
        if (isErrorResponse) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated Error Response")
        } else {
            return AuthResponse("TestToken", "TestAuthToken", 300)
        }
    }

    fun setErrorResponse(error: Boolean) {
        isErrorResponse = error
    }

    fun getRequestCount() =
        requestCount.get()

    fun reset() {
        requestCount.set(0)
        isErrorResponse = false
    }
}