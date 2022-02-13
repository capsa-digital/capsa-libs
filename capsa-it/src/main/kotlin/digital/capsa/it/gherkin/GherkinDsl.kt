package digital.capsa.it.gherkin

fun <S> given(block: () -> S): Given<S> = Given(block())
fun <R> on(block: () -> R): On<R> = On(block())
fun <R> onError(block: () -> R): On<Throwable> {
    return try {
        block()
        On(Throwable("No error encountered"))
    } catch (e: Throwable) {
        On(e)
    }
}

class Given<S>(private val setup: S) {
    fun <R> on(block: S.() -> R): On<R> = On(block(setup))
    fun <R> onError(block: (S) -> R): On<Throwable> {
        return try {
            block(setup)
            On(Throwable("No error encountered"))
        } catch (e: Throwable) {
            On(e)
        }
    }
}

class On<R>(private val result: R) {
    fun <O> then(block: R.() -> O): Then<O> = Then(block(result))
}

class Then<O>(private val result: O)

