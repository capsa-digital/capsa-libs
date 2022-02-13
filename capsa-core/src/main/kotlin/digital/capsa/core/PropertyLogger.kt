package digital.capsa.core

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.Environment
import java.util.*
import java.util.stream.StreamSupport

object PropertyLogger {
    fun printPropertiesOnStartup() {
        if (logger.isDebugEnabled) {
            val buffer = StringBuffer("============ System Properties ============\n")

            val properties = System.getProperties()
            properties.forEach { k, v -> buffer.append("$k = ${sanitizePropertyValue(k.toString(), v.toString())}\n") }

            buffer.append("============ env Properties ============\n")

            val env = System.getenv()
            env.forEach { (k, v) -> buffer.append("$k = $v\n") }

            logger.debug("Environment:\n$buffer\n")
        }
    }

    fun printPropertiesOnReady(event: ApplicationReadyEvent) {
        if (logger.isDebugEnabled) {
            val buffer = StringBuffer("============ configuration ============\n")
            val contextEnv: Environment = event.getApplicationContext().getEnvironment()
            buffer.append("Active profiles: {}", Arrays.toString(contextEnv.getActiveProfiles()))
            val sources = (contextEnv as AbstractEnvironment).propertySources
            StreamSupport.stream(sources.spliterator(), false)
                .filter { ps -> ps is EnumerablePropertySource<*> }
                .map { ps -> (ps as EnumerablePropertySource<*>).propertyNames }
                .flatMap(Arrays::stream)
                .distinct()
                .forEach { prop ->
                    buffer.append(
                        "$prop: ${
                            sanitizePropertyValue(
                                prop,
                                contextEnv.getProperty(prop)
                            )
                        }\n"
                    )
                }
            buffer.append("===========================================")
            logger.debug("Environment:\n$buffer")
        }
    }

    private fun sanitizePropertyValue(name: String, value: String?): String? {
        return if ((name.contains("password", ignoreCase = true)
                    || name.contains("secret", ignoreCase = true)) && value?.isNotBlank() != false
        ) {
            "*****"
        } else {
            value
        }
    }
}