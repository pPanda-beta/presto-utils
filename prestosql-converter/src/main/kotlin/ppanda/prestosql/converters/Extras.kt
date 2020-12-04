package ppanda.prestosql.converters

import io.prestosql.sql.tree.Identifier
import io.prestosql.sql.tree.NodeLocation
import io.prestosql.sql.tree.QualifiedName


open class EmptyIdentifier : Identifier {
    constructor() : super("emptyIdentifier", false)
    constructor(location: NodeLocation) : super(location, "emptyIdentifier", false)

    override fun getValue(): String = ""

    companion object {
        @JvmStatic
        fun create() = EmptyIdentifier()

        @JvmStatic
        fun createAsQn(): QualifiedName = QualifiedName.of(listOf(create()))
    }
}

