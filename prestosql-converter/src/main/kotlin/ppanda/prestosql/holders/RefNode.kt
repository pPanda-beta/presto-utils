package ppanda.prestosql.holders

import io.prestosql.sql.tree.AstVisitor
import io.prestosql.sql.tree.Node
import java.util.*


class RefNode<T : Node>(val actualNode: T?) : Node(actualNode?.location) {

    override fun getChildren(): List<Node> = actualNode?.let { listOf(it) } ?: emptyList()

    override fun <R, C> accept(visitor: AstVisitor<R, C>, context: C): R = visitor.process(actualNode, context)

    override fun toString(): String = """RefNode{actualNode=$actualNode}"""

    override fun hashCode(): Int = Objects.hash(actualNode)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        if (other is RefNode<*> && actualNode == other.actualNode) {
            return true
        }
        return false
    }
}

