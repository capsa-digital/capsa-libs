package digital.capsa.it.dsl

fun <S> given(block: () -> S): Given<S> =
        Given(block())

class Given<S>(private val setup: S) {

    fun <R> on(block: (S) -> R): On<R> =
            On(block(setup))
}

class On<R>(private val result: R) {

    fun <O> then(block: (R) -> O): Then<O> =
            Then(block(result))
}

class Then<O>(private val result: O)

