package ppanda.prestosql.converters

import io.prestosql.sql.tree.Identifier
import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.NodeLocation
import io.prestosql.sql.tree.SingleColumn
import ppanda.prestosql.contexts.ContextWithPath
import java.util.*

open class ColumnNameBlockQuote : SqlConverter<SingleColumn>() {
    override fun visitSingleColumn(column: SingleColumn, context: ContextWithPath<*, Node>): SingleColumn {
        val expression = column.expression
        val newAlias: Optional<Identifier> = column.alias.map { id: Identifier -> BlockQuoteIdentifier(id) }
        return if (column.location.isPresent) {
            SingleColumn(column.location.get(), expression, newAlias)
        } else SingleColumn(expression, newAlias)
    }
}

open class BlockQuoteIdentifier : Identifier {
    constructor(id: Identifier) : super(id.value, false) {}
    constructor(location: NodeLocation?, id: Identifier) : super(location, id.value, false) {}

    override fun getValue(): String {
        return "`" + super.getValue() + "`"
    }

    companion object {
        fun wrap(id: Identifier): BlockQuoteIdentifier {
            return if (id.location.isPresent) BlockQuoteIdentifier(id.location.get(), id) else BlockQuoteIdentifier(id)
        }
    }
}

