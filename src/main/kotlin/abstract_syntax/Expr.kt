package org.sdfg610.dims.abstract_syntax

/* When matching on a sealed interface in a switch-case, Kotlin will warn you if you forgot to match a possible case */
sealed interface Expr {
    val lineNumber: Int
}

class UnaryOp(var op: UnaryOperators, var expr: Expr, override val lineNumber: Int) : Expr

class BinaryOp(var op: BinaryOperators, var exprLeft: Expr, var exprRight: Expr, override val lineNumber: Int) : Expr


class Ref(var name: Var, override val lineNumber: Int) : Expr

class BoolV(var value: Boolean, override val lineNumber: Int) : Expr

class NumV(var value: Num, override val lineNumber: Int) : Expr


enum class UnaryOperators {
    NOT, NEG
}

enum class BinaryOperators {
    ADD, SUB, MUL, LT, EQ, OR
}