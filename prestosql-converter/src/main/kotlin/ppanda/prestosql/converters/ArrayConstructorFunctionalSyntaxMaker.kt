package ppanda.prestosql.converters

import io.prestosql.sql.tree.*
import ppanda.prestosql.contexts.ContextWithPath


open class ArrayConstructorFunctionalSyntaxMaker : SqlConverter<Expression>() {
    override fun visitArrayConstructor(node: ArrayConstructor, context: ContextWithPath<*, Node>?): Expression {
        return node.location
                .map { FunctionCall(it, QualifiedName.of("array"), node.values) }
                .orElseGet { FunctionCall(QualifiedName.of("array"), node.values) }
    }
}

