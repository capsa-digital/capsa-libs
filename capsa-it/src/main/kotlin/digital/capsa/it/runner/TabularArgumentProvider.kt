package digital.capsa.it.runner

import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer

class TabularArgumentProvider : ArgumentsProvider, AnnotationConsumer<TabularSource?> {

    private var annotation: TabularSource? = null
    var index = AtomicLong(0)

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return this.annotation!!.value.trimMargin().lines()
            .map { line ->
                line.split('|').dropLast(1).map { it.trim() }.toTypedArray()
            }.stream().map { Arguments.of(*it) }
    }

    override fun accept(annotation: TabularSource?) {
        this.annotation = annotation
    }

}
