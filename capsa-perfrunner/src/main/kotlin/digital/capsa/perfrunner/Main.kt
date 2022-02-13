package digital.capsa.perfrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Main

fun main(vararg args: String) {
    runApplication<Main>(*args)
}