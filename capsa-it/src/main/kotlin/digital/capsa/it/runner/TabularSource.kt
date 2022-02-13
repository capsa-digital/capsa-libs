package digital.capsa.it.runner

import org.apiguardian.api.API
import org.junit.jupiter.params.provider.ArgumentsSource

@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@API(status = API.Status.EXPERIMENTAL, since = "5.0")
@ArgumentsSource(TabularArgumentProvider::class)
annotation class TabularSource(
    val value: String
)