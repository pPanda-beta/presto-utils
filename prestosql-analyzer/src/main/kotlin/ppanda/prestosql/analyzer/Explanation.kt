package ppanda.prestosql.analyzer

import io.prestosql.sql.tree.*
import ppanda.prestosql.analyzer.extractors.extractAliases
import ppanda.prestosql.analyzer.extractors.extractColumns
import ppanda.prestosql.analyzer.extractors.extractNodes
import ppanda.prestosql.analyzer.extractors.extractTables
import ppanda.prestosql.analyzer.visitors.DepthFirstVisitor

object Explanation {
    fun getLineageInfo(statement: Statement): Map<Identifier, Collection<DereferenceExpression>> {
        val aliasVisitor = DepthFirstVisitor.by(extractAliases())
        val aliases = statement.accept(aliasVisitor, null)
                .associateBy({ it.alias!! }, { it.relation!! })

        //TODO: alias could be referring to multiple tables
        val resolveAlias = { dependencyExpr: DereferenceExpression ->
            DereferenceExpression(
                    Identifier(aliases[dependencyExpr.base].toString()),
                    dependencyExpr.field
            )
        }

        val findDependencies = { col: SingleColumn ->
            getAllNodes(col, DereferenceExpression::class.java).map(resolveAlias)
        }

        val outputColumns = extractColumnExprs(statement)
        return outputColumns
                .filter { it is SingleColumn && it.alias.isPresent }
                .map { it as SingleColumn }
                .sortedBy { it.alias.toString() }
                .associateBy({ it.alias.get() }, { findDependencies(it).toList() })

    }

    fun getAllColumns(statement: Statement) =
            statement.accept(DepthFirstVisitor.by(extractColumns()), null).toList()


    fun getDependencyTables(statement: Statement) =
            statement.accept(DepthFirstVisitor.by(extractTables()), null).toList()

    fun <N : Node> getAllNodes(root: Node, classOfN: Class<N>): Sequence<N> =
            DepthFirstVisitor.by(extractNodes(classOfN))
                    .process(root)

    private fun extractColumnExprs(statement: Statement): Sequence<SelectItem> {
        return getAllNodes(statement, QuerySpecification::class.java)
                .flatMap { it.select.selectItems.asSequence() }
    }

}