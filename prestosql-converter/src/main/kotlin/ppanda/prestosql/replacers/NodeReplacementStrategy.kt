package ppanda.prestosql.replacers

import io.prestosql.sql.tree.Node


interface NodeReplacementStrategy<P : Node, N : Node> {
    fun replace(parent: P, oldChild: N, newChild: N)
}

