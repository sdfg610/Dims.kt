package org.sdfg610.dims.interpretation

import org.sdfg610.dims.abstract_syntax.Var

class EnvV(
    private val parentScope: EnvV? = null,
    private val bindings: MutableMap<Var, Val?> = HashMap()
) {
    fun newScope(): EnvV // Scopes are exited by simply remembering the parent-scope-reference in the calling code
            = EnvV(this)

    fun bind(variable: Var, value: Val?) {
        if (isLocal(variable))
            throw Exception("Variable '$variable' already bound in local scope.")
        bindings[variable] = value
    }

    fun set(variable: Var, value: Val?) {
        if (bindings.containsKey(variable))
            bindings[variable] = value;
        else if (parentScope != null)
            parentScope.set(variable, value)
        else
            throw Exception("Cannot overwrite value of un-bound identifier '$variable'.")
    }

    fun tryGet(variable: Var): Val?
            = if (isLocal(variable)) bindings[variable] else parentScope?.tryGet(variable)

    fun isLocal(variable: Var): Boolean
            = bindings.containsKey(variable)
}