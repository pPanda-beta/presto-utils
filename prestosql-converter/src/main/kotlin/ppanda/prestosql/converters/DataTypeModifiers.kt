package ppanda.prestosql.converters

import io.prestosql.sql.tree.GenericDataType
import io.prestosql.sql.tree.Identifier
import io.prestosql.sql.tree.Node
import ppanda.prestosql.contexts.ContextWithPath

open class DataTypeModifiers(
        mappers: Map<String, NodeMapper<GenericDataType>>
) : SqlConverter<GenericDataType>() {
    val normalizedMappers = mappers.mapKeys { it.key.toUpperCase() }

    override fun visitGenericDataType(node: GenericDataType, context: ContextWithPath<*, Node>): GenericDataType {
        return normalizedMappers[node.name.toString().toUpperCase()]
                ?.invoke(node, context)
                ?: node
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun replaceBy(newType: String, delimited: Boolean = false): NodeMapper<GenericDataType> = { oldType, _ ->
            GenericDataType(
                    oldType.location,
                    oldType.name.location.map { Identifier(it, newType, delimited) }
                            .orElseGet { Identifier(newType, delimited) },
                    oldType.arguments
            )
        }
    }
}

