package org.sdfg610.dims.abstract_syntax

/* When matching on a sealed interface in a switch-case, Kotlin will warn you if you forgot to match a possible case */
sealed interface Type

data object BoolT : Type

data object IntT : Type
