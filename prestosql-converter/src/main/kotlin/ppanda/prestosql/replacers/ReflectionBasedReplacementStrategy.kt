package ppanda.prestosql.replacers

import io.prestosql.sql.tree.Node
import java.lang.reflect.Field


open class ReflectionBasedReplacementStrategy<P : Node, C : Node> : NodeReplacementStrategy<P, C> {
    override fun replace(parent: P, oldChild: C, newChild: C) {
        val visited = mutableSetOf<Any>()
        ReplacerImpl<C>(oldChild, newChild, visited)
                .replaceInside(parent)
    }
}

// Recursive depth first replacement strategy
private class ReplacerImpl<C : Node>(
        private val oldChild: C?,
        private val newChild: C?,
        private val visited: MutableSet<Any>) {

    fun replaceInside(holder: Any?) {
        if (holder == null || visited.contains(holder)) {
            return
        }
        visited.add(holder)

        if (holder.javaClass.isArray && !holder.javaClass.componentType.isPrimitive) {
            val replaceDone = replaceOnArray(holder)
            if (replaceDone) return
        }

        for (field in holder.javaClass.declaredFields) {
            val replaceDone = replaceOnField(holder, field)
            if (replaceDone) return
        }
    }

    private fun replaceOnArray(holder: Any?): Boolean {
        val arrayHolder = holder as Array<Any?>
        if (oldChild in arrayHolder) {
            val indexOfOldChild = arrayHolder.indexOf(oldChild)
            arrayHolder[indexOfOldChild] = newChild
            return true
        }
        return false
    }

    private fun replaceOnField(holder: Any?, field: Field): Boolean {
//        try {
        field.isAccessible = true
        val fieldCurrentRef = field[holder]
        if (fieldCurrentRef === oldChild) {
            field[holder] = newChild
            return true
        }
        replaceInside(fieldCurrentRef)
//        } catch (e: IllegalAccessException) {
//            e.printStackTrace()
//        }
        return false
    }

}