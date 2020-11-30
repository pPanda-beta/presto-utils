package ppanda.prestosql.converters

import io.prestosql.sql.tree.AstVisitor
import io.prestosql.sql.tree.Node
import ppanda.prestosql.contexts.ContextWithPath


abstract class SqlConverter<ConvertedT : Node>
    : AstVisitor<ConvertedT?, ContextWithPath<*, Node>>()


//TODO: Bounds are not forced, T can be anything
typealias NodeMapper<T> = (T, ContextWithPath<*, Node>) -> T

//TODO: Extract N-ary partial function
interface PartialBiFunction<T1, T2, R> : (T1, T2) -> R? {
    fun isApplicableFor(t1: T1, t2: T2): Boolean
    fun apply(t1: T1, t2: T2): R
    override fun invoke(t1: T1, t2: T2): R? = if (isApplicableFor(t1, t2)) apply(t1, t2) else null

    companion object {
        fun <T1, T2, R> using(predicate: (T1, T2) -> Boolean, func: (T1, T2) -> R) =
                object : PartialBiFunction<T1, T2, R> {
                    override fun isApplicableFor(t1: T1, t2: T2): Boolean = predicate(t1, t2)
                    override fun apply(t1: T1, t2: T2): R = func(t1, t2)
                }
    }
}

interface ConditionalNodeMapper<T : Node> : PartialBiFunction<T, ContextWithPath<*, Node>, T> {
    fun isApplicableFor(t: T): Boolean
    override fun isApplicableFor(t1: T, ctx: ContextWithPath<*, Node>): Boolean = isApplicableFor(t1)

    companion object {
        fun <T : Node> using(predicate: (T) -> Boolean, func: (T, ContextWithPath<*, Node>) -> T) =
                object : ConditionalNodeMapper<T> {
                    override fun isApplicableFor(t: T): Boolean = predicate(t)
                    override fun apply(t1: T, ctx: ContextWithPath<*, Node>): T = func(t1, ctx)
                }
    }
}