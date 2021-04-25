package digital.capsa.core.auth

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)
