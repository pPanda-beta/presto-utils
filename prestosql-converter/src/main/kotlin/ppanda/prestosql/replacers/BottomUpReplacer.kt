package ppanda.prestosql.replacers

import io.prestosql.sql.tree.AstVisitor
import io.prestosql.sql.tree.Node
import ppanda.prestosql.contexts.ContextWithPath
import ppanda.prestosql.converters.SqlConverter
import ppanda.prestosql.holders.RefNode

typealias ReplacementContext = ContextWithPath<NodeReplacementStrategy<Node, Node>, Node>
typealias ReplacementStrategy = NodeReplacementStrategy<Node, Node>

open class BottomUpReplacer(
        private val converters: List<SqlConverter<out Node>>)
    : AstVisitor<Void?, ReplacementContext>() {


    fun <T : Node> replaceAll(rootNode: T, strategy: ReplacementStrategy): T {
        val refNode = RefNode(rootNode)

        val parentOfRootContext: ReplacementContext = ContextWithPath.initial(strategy, refNode)
        val rootContext: ReplacementContext = parentOfRootContext.next(rootNode)
        process(rootNode, rootContext)
        return refNode.actualNode!!
    }

    override fun visitNode(node: Node, contextWithPath: ReplacementContext): Void? {
        node.children.forEach { child: Node -> process(child, contextWithPath.next(child)) }
        replaceCurrentNode(node, contextWithPath)
        return null
    }

    private fun replaceCurrentNode(node: Node, contextWithPath: ReplacementContext) {
        val convertedNode = applyAllConverters(node, contextWithPath)

        val parentNode = contextWithPath.parentNode
        val replacementStrategy = contextWithPath.context
        replacementStrategy.replace(parentNode, node, convertedNode)
    }

    private fun applyAllConverters(node: Node, contextWithPath: ReplacementContext): Node = converters.fold(node) { convertedNodeSoFar, converter ->
        converter.process(convertedNodeSoFar, contextWithPath) ?: convertedNodeSoFar
    }

}