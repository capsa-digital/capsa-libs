package digital.capsa.it.aggregate

import digital.capsa.core.logger
import digital.capsa.it.TestContext
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

interface Aggregate {
    var key: Key?

    var id: UUID?

    var parent: Aggregate?

    val children: List<Aggregate>

    var randomSeed: Long

    fun create(context: TestContext)

    fun onCreate()

    fun toString(builder: StringBuilder, nesting: Int)

    fun getChildCount(kClass: KClass<out Aggregate>): Int

    fun construct()

    fun getDescendantCount(kClass: KClass<out Aggregate>): Int
}

inline fun <reified T> Aggregate.getChild(i: Int): T {
    return children.filter { it is T }[i] as T
}

inline fun <reified T> Aggregate.getChild(key: Key): T {
    return children.filter { it is T && key == it.key }[0] as T
}

@DslMarker
annotation class AggregateMarker

data class Key(var value: String)

@AggregateMarker
abstract class AbstractAggregate : Aggregate {

    lateinit var context: TestContext

    override var key: Key? = null

    override var id: UUID? = null

    override var parent: Aggregate? = null

    override val children = arrayListOf<Aggregate>()

    override var randomSeed: Long = 0

    protected fun <T : Aggregate> initAggregate(aggregate: T, init: T.() -> Unit): T {
        aggregate.parent = this
        aggregate.construct()
        children.add(aggregate)
        aggregate.init()
        return aggregate
    }

    override fun create(context: TestContext) {
        this.context = context
        if (parent == null) {
            logger.info("Aggregate Tree:\n $this")
        }
        onCreate()
        for (c in children) {
            c.create(context)
        }
    }

    override fun toString(builder: StringBuilder, nesting: Int) {
        builder.append("".padStart(nesting * 4) + "${this::class.simpleName} ${getAttributes()}\n")
        for (c in children) {
            c.toString(builder, nesting + 1)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        toString(builder, 0)
        return builder.toString()
    }

    open fun getAttributes(): String {
        return this::class.declaredMemberProperties.filter { it.visibility == KVisibility.PUBLIC }
            .joinToString(prefix = "[", postfix = "]") { "${it.name}=${it.getter.call(this)}" }
    }

    override fun getChildCount(kClass: KClass<out Aggregate>): Int {
        return children.count { kClass.isInstance(it) }
    }

    override fun getDescendantCount(kClass: KClass<out Aggregate>): Int {
        fun count(aggregate: Aggregate, kClass: KClass<out Aggregate>): Int {
            return getChildCount(kClass) + aggregate.children.sumOf { it.getDescendantCount(kClass) }
        }
        return count(this, kClass)
    }

    inline fun <reified T : Aggregate> getDescendant(key: Key): T? {
        return getRecursiveDescendant(this, T::class, key)
    }

    fun <T : Aggregate> getRecursiveDescendant(aggregate: Aggregate, kClass: KClass<out T>, key: Key): T? {
        val children = aggregate.children.filter { it::class == kClass && key == it.key }
        return if (children.isNotEmpty()) {
            children[0] as T
        } else {
            var child: T? = null
            for (item in aggregate.children) {
                child = getRecursiveDescendant(item, kClass, key)
                if (child != null) {
                    break
                }
            }
            child
        }
    }

    inline fun <reified T : Aggregate> getAncestor(): T? {
        return getRecursiveAncestor(this, T::class)
    }

    fun <T : Aggregate> getRecursiveAncestor(aggregate: Aggregate, kClass: KClass<out T>): T? {
        return if (aggregate.parent == null) {
            null
        } else if (aggregate.parent!!::class == kClass) {
            aggregate.parent!! as T
        } else {
            getRecursiveAncestor(aggregate.parent!!, kClass)
        }
    }

    operator fun String.unaryMinus() {
        key = Key(this)
    }

    fun nextRandomInt(seed: Int, bound: Int): Int {
        return Random(31 * randomSeed + seed).nextInt(bound)
    }

}


