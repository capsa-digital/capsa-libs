package digital.capsa.core.exceptions

open class CatastrophicFailureException(message: String, t: Throwable) : RuntimeException(message, t)

class AuthTokenException(message: String, t: Throwable?) : RuntimeException(message, t)
