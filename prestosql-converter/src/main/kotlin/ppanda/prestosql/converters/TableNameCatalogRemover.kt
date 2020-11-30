package ppanda.prestosql.converters

import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.QualifiedName
import io.prestosql.sql.tree.Table
import ppanda.prestosql.contexts.ContextWithPath

open class TableNameCatalogRemover : SqlConverter<Table>() {
    override fun visitTable(node: Table, context: ContextWithPath<*, Node>): Table =
            QualifiedName.of(node.name.originalParts.takeLast(2))
                    .let { newName ->
                        node.location
                                .map { Table(it, newName) }
                                .orElseGet { Table(newName) }
                    }

}
