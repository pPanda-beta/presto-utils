package ppanda.prestosql.converters

import io.prestosql.sql.tree.*
import ppanda.prestosql.contexts.ContextWithPath
import java.util.*

open class UnnestModifier(
        private val replacementFnName: String = "explode"
) : SqlConverter<Node>() {
    override fun visitAliasedRelation(node: AliasedRelation, context: ContextWithPath<*, Node>?): Node? {
        if (!isApplicable(node, context)) {
            return super.visitAliasedRelation(node, context)
        }
        return Unnest::class.java.cast(node.relation)
                .let { convertUnnestIntoSelectExpr(it, node) }
    }

    protected open fun isApplicable(node: AliasedRelation, context: ContextWithPath<*, Node>?) = node.relation is Unnest

    private fun convertUnnestIntoSelectExpr(unnestNode: Unnest, node: AliasedRelation): TableSubquery {
        val expressions = unnestNode.expressions
        val selectExpressions = expressions.zip(node.columnNames)
                .map { (expr, colName) ->
                    SingleColumn(
                            FunctionCall(QualifiedName.of(replacementFnName), listOf(expr)),
                            colName
                    )
                }
        return asSelectSubQuery(node.location, selectExpressions)
    }

    private fun asSelectSubQuery(location: Optional<NodeLocation>, selectExpressions: List<SingleColumn>): TableSubquery {
        val select = Select(false, selectExpressions)
        val querySpec = QuerySpecification(select, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
        val query = Query(Optional.empty(), querySpec, Optional.empty(), Optional.empty(), Optional.empty())
        return location
                .map { TableSubquery(it, query) }
                .orElseGet { TableSubquery(query) }
    }
}