package org.sdfg610.dims.semantic_analysis

import org.sdfg610.dims.abstract_syntax.*

class EnvAT(
    private val parentScope: EnvAT? = null,
    private val bindings: MutableMap<Var, AT> = HashMap()
) {
    fun newScope(): EnvAT // Scopes are exited by simply remembering the parent-scope-reference in the calling code
            = EnvAT(this)

    fun bind(variable: Var, value: AT) {
        if (isLocal(variable))
            throw Exception("Variable '$variable' already bound in local scope.")
        bindings[variable] = value
    }

    fun set(variable: Var, value: AT) {
        if (bindings.containsKey(variable))
            bindings[variable] = value;
        else if (parentScope != null)
            parentScope.set(variable, value)
        else
            throw Exception("Cannot overwrite value of un-bound identifier '$variable'.")
    }

    fun tryGet(variable: Var): AT?
            = bindings[variable] ?: parentScope?.tryGet(variable)

    fun isLocal(variable: Var): Boolean
            = bindings.containsKey(variable)


    fun domain(): List<Var> = bindings.keys.union<Var>(parentScope?.domain() ?: emptyList()).toList()

    fun clone(): EnvAT {
        val newMap = HashMap<Var, AT>()
        for (x in bindings.entries)
            newMap.put(x.key, x.value.clone())

        return EnvAT(parentScope?.clone(), newMap)
    }
}