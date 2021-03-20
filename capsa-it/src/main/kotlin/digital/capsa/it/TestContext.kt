package digital.capsa.it

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment

class TestContext(val applicationContext: ApplicationContext) {

    val memento: HashMap<String, String> = HashMap()

    val objectMapper = applicationContext.getBean(ObjectMapper::class.java)

    val environment = applicationContext.getBean(Environment::class.java)

}