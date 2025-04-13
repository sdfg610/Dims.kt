package org.sdfg610.dims.interpretation

sealed interface Val {
    fun asInt() : Int = (this as IntVal).n
    fun asBool() : Boolean = (this as BoolVal).b
}

data class IntVal(val n: Int) : Val {
    override fun toString() = n.toString()
}
data class BoolVal(val b: Boolean) : Val {
    override fun toString() = b.toString()
}
