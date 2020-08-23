package ppanda.prestosql.converters

import io.prestosql.sql.tree.Expression
import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.TryExpression
import ppanda.prestosql.contexts.ContextWithPath

open class TryUnpacker : SqlConverter<Expression>() {
    override fun visitTryExpression(node: TryExpression, contextWithPath: ContextWithPath<*, Node>): Expression {
        return node.innerExpression
    }
}