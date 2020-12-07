package ppanda.prestosql.converters

import io.prestosql.sql.tree.Expression
import io.prestosql.sql.tree.FunctionCall
import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.Row
import ppanda.prestosql.contexts.ContextWithPath


open class ValuesRowUnpacker : SqlConverter<Expression>() {
    //TODO shoul check if the parent is "Values" node or not
    override fun visitRow(node: Row, context: ContextWithPath<*, Node>?): Expression? {
        return node.location
                .map { FunctionCall(it, EmptyIdentifier.createAsQn(), node.items) }
                .orElseGet { FunctionCall(EmptyIdentifier.createAsQn(), node.items) }
    }
}

