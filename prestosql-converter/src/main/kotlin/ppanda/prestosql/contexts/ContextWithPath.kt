package ppanda.prestosql.contexts

class ContextWithPath<C, N>(
        val parent: ContextWithPath<C, N>?,
        val context: C,
        val node: N) {

    fun next(nextNode: N): ContextWithPath<C, N> {
        return next(context, nextNode)
    }

    fun next(nextContext: C, nextNode: N): ContextWithPath<C, N> {
        return ContextWithPath(this, nextContext, nextNode)
    }

    val parentNode: N
        get() = parent!!.node

    companion object {
        fun <C, N> initial(context: C, node: N): ContextWithPath<C, N> {
            return ContextWithPath(null, context, node)
        }
    }

}