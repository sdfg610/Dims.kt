package org.sdfg610.dims.abstract_syntax

/* When matching on a sealed interface in a switch-case, Kotlin will warn you if you forgot to match a possible case */
sealed interface Expr

class UnaryOp(var op: UnaryOperators, var expr: Expr) : Expr

class BinaryOp(var op: BinaryOperators, var exprLeft: Expr, var exprRight: Expr) : Expr


class Ref(var name: Var) : Expr

class BoolV(var value: Boolean) : Expr

class NumV(var value: Num) : Expr


enum class UnaryOperators {
    NOT, NEG
}

enum class BinaryOperators {
    ADD, SUB, MUL, LT, EQ, OR
}