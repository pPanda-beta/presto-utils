package ppanda.prestosql.analyzer.visitors

import io.prestosql.sql.tree.AstVisitor
import io.prestosql.sql.tree.Node
import ppanda.prestosql.analyzer.extractors.allChildren

open class DepthFirstVisitor<R : Any, C>(private val visitor: AstVisitor<R?, C?>) : AstVisitor<Sequence<R>, C?>() {
    public override fun visitNode(node: Node, context: C?): Sequence<R> {
        val nodeResult = sequenceOf(visitor.process(node, context))
        val childrenResult = node.allChildren()
                .asSequence()
                .flatMap { child -> process(child, context) }

        return (nodeResult + childrenResult).filterNotNull()
    }

    companion object {
        @JvmStatic
        fun <R : Any, C> by(visitor: AstVisitor<R?, C?>) = DepthFirstVisitor(visitor)
    }
}
