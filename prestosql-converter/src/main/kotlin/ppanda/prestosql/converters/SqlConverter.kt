package ppanda.prestosql.converters

import io.prestosql.sql.tree.AstVisitor
import io.prestosql.sql.tree.Node
import ppanda.prestosql.contexts.ContextWithPath


abstract class SqlConverter<ConvertedT : Node>
    : AstVisitor<ConvertedT?, ContextWithPath<*, Node>>()


