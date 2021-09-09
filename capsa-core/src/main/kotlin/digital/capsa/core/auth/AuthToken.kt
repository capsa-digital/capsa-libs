package digital.capsa.core.auth

import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger

data class AuthToken(
    val token: String,
    val tokenType: String,
    val expiryDate: ZonedDateTime,
    val usageCounter: AtomicInteger = AtomicInteger(0)
)
