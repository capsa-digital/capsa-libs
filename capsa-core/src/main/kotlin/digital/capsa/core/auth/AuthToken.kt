package digital.capsa.core.auth

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

data class AuthToken(
    val token: String,
    val tokenType: String,
    val expiryDate: Instant,
    val usageCounter: AtomicInteger = AtomicInteger(0)
)
