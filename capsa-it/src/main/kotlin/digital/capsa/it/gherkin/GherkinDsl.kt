package digital.capsa.it.gherkin

import digital.capsa.it.dsl.Given
import digital.capsa.it.dsl.On
import digital.capsa.it.dsl.Then

fun <S> given(block: () -> S): Given<S> = Given(block())

class Given<S>(private val setup: S) {
    fun <R> on(block: (S) -> R): On<R> = On(block(setup))
}

class On<R>(private val result: R) {
    fun <O> then(block: (R) -> O): Then<O> = Then(block(result))
}

class Then<O>(private val result: O)

