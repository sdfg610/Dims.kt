package org.sdfg610.dims.common

import org.sdfg610.dims.abstract_syntax.Var

open class Env<T> (
    private val parentScope: Env<T>? = null
) {
    private val bindings: MutableMap<Var, T> = HashMap()


    fun newScope(): Env<T> // Scopes are exited by simply remembering the parent-scope-reference in the calling code
    = Env(this)

    fun bind(variable: Var, value: T) {
        if (isLocal(variable))
            throw Exception("Variable '$variable' already bound in local scope.")
        bindings[variable] = value
    }

    fun set(variable: Var, value: T) {
        if (bindings.containsKey(variable))
            bindings[variable] = value;
        else if (parentScope != null)
            parentScope.set(variable, value)
        else
            throw Exception("Cannot overwrite value of un-bound identifier '$variable'.")
    }

    fun tryGet(variable: Var): T?
        = bindings[variable] ?: parentScope?.tryGet(variable)

    fun isLocal(variable: Var): Boolean
        = bindings.containsKey(variable)
}