package ppanda.prestosql.converters

import io.prestosql.sql.tree.Identifier
import io.prestosql.sql.tree.NodeLocation
import io.prestosql.sql.tree.QualifiedName

//TODO: Can be implemented by extending UnregulatedIdentifier("")
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

open class UnregulatedIdentifier(val unconditionalValue: String) : Identifier("unregulatedIdentifier", false) {
    override fun getValue(): String = unconditionalValue

    companion object {
        @JvmStatic
        fun create(value: String) = UnregulatedIdentifier(value)

        @JvmStatic
        fun createAsQn(value: String): QualifiedName = QualifiedName.of(listOf(create(value)))
    }
}