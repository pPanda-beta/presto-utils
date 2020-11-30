@file:JvmName("Extractors")

package ppanda.prestosql.analyzer.extractors

import io.prestosql.sql.tree.*


fun extractTables() = object : AstVisitor<Table?, Any?>() {
    override fun visitTable(node: Table, context: Any?): Table {
        return node
    }
}

fun extractColumns() = object : AstVisitor<SingleColumn?, Any?>() {
    override fun visitSingleColumn(node: SingleColumn, context: Any?): SingleColumn {
        return node
    }
}

fun extractAliases() = object : AstVisitor<AliasedRelation?, Any?>() {
    override fun visitAliasedRelation(node: AliasedRelation, context: Any?): AliasedRelation {
        return node
    }
}

fun <N : Node> extractNodes(classOfN: Class<N>) = object : AstVisitor<N?, Any?>() {
    override fun visitNode(node: Node, context: Any?): N? {
        return if (classOfN.isInstance(node)) {
            node as N
        } else super.visitNode(node, context)
    }
}


fun Node.allChildren(): List<Node> = children + when (this) {
    //TODO: Somehow those exceptional children are not included in AST
    is Cast -> listOf(type)
    else -> emptyList()
}
