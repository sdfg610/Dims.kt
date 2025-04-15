package org.sdfg610.dims.semantic_analysis

import org.sdfg610.dims.abstract_syntax.Type

data class AT(var isAssigned: Boolean, val type: Type) {
    fun clone(): AT = AT(isAssigned, type)
}