package ppanda.prestosql.converters

import io.prestosql.sql.tree.DecimalLiteral
import io.prestosql.sql.tree.Expression
import io.prestosql.sql.tree.Node
import ppanda.prestosql.contexts.ContextWithPath


class DecimalLiteralConverter : SqlConverter<Expression>() {
    override fun visitDecimalLiteral(node: DecimalLiteral, context: ContextWithPath<*, Node>?): Expression {
        return UnregulatedIdentifier.create(node.value + "d")
    }
}