package ppanda.prestosql.converters

import io.prestosql.sql.tree.Expression
import io.prestosql.sql.tree.FunctionCall
import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.QualifiedName
import ppanda.prestosql.contexts.ContextWithPath

open class FunctionTranslators(
        val mappers: List<ConditionalNodeMapper<FunctionCall>>
) : SqlConverter<FunctionCall>() {

    @JvmOverloads
    constructor(mappers: Map<String, NodeMapper<FunctionCall>>,
                predicateBasedMappers: List<ConditionalNodeMapper<FunctionCall>> = emptyList()
    ) : this(predicateBasedMappers
            + mappers.entries.map {
        ConditionalNodeMapper.using({ node -> it.key == node.name.toString() }, it.value)
    })

    override fun visitFunctionCall(node: FunctionCall, context: ContextWithPath<*, Node>): FunctionCall =
            mappers.find { it.isApplicableFor(node) }?.apply(node, context) ?: node


    companion object {
        @JvmStatic
        fun justRenameTo(newFuncName: QualifiedName): NodeMapper<FunctionCall> = { fn, _ ->
            FunctionCall(fn.location, newFuncName, fn.window, fn.filter, fn.orderBy, fn.isDistinct, fn.nullTreatment, fn.arguments)
        }

        @JvmStatic
        fun justRenameTo(first: String, vararg remainingParts: String) =
                justRenameTo(QualifiedName.of(first, *remainingParts))

        @JvmStatic
        fun byExpressionGen(exprGen: (FunctionCall, ContextWithPath<*, Node>) -> Expression): NodeMapper<FunctionCall> = { fn, ctx ->
            buildCopyWith(fn, EmptyIdentifier.createAsQn(), exprGen(fn, ctx))
        }

        @JvmStatic
        fun buildCopyWith(fn: FunctionCall, newFuncName: QualifiedName, expression: Expression) =
                FunctionCall(fn.location, newFuncName, fn.window, fn.filter, fn.orderBy, fn.isDistinct, fn.nullTreatment, listOf(expression))
    }
}

