package org.sdfg610.dims.pretty_printing

import org.sdfg610.dims.abstract_syntax.*

class PrettyPrinter {
    companion object {
        fun printStmt(stmt: Stmt?, depth: Int = 0): String =
            when(stmt) {
                null, Skip     -> ""
                is Comp        -> printStmt(stmt.stmt1, depth) + "\n" + printStmt(stmt.stmt2, depth)
                is Declaration -> indent(depth) + printType(stmt.type) + " " + stmt.identifier + ";"
                is Assign      -> indent(depth) + stmt.identifier + " := " + printExpr(stmt.value) + ";"
                is Print       -> indent(depth) + "print " + printExpr(stmt.value) + ";"
                is If          -> {
                    indent(depth) + "if (" + printExpr(stmt.condition) + ") then \n" + printStmt(stmt.thenBody, depth + 1) + "\n" + (
                        if (stmt.elseBody != Skip)
                            indent(depth) + "else\n" + printStmt(stmt.elseBody, depth + 1) + "\n"
                        else
                            ""
                    ) + indent(depth) + "endif\n"
                }
                is While       -> {
                    indent(depth) + "while (" + printExpr(stmt.condition) + ") do \n" + (
                        printStmt(stmt.body, depth + 1)
                    ) + "\n" + indent(depth) + "endwhile\n"
                }
            }

        fun printExpr(expr: Expr?): String =
            when(expr) {
                null -> ""
                is UnaryOp -> unaryOpString(expr.op) + surround(expr.expr)
                is BinaryOp -> surround(expr.exprLeft) + binaryOpString(expr.op) + surround(expr.exprRight)
                is Ref -> expr.name
                is BoolV -> expr.value.toString()
                is NumV -> expr.value.toString()
            }

        fun printType(type: Type?): String =
            when(type) {
                null -> ""
                BoolT -> "bool"
                IntT -> "int"
            }


        private fun indent(depth: Int)
        = "    ".repeat(depth)

        fun binaryOpString(op: BinaryOperators): String =
            when (op) {
                BinaryOperators.ADD -> " + "
                BinaryOperators.SUB -> " - "
                BinaryOperators.MUL -> " * "
                BinaryOperators.LT -> " < "
                BinaryOperators.EQ -> " = "
                BinaryOperators.OR -> " || "
            }

        fun unaryOpString(op: UnaryOperators): String =
            when (op) {
                UnaryOperators.NOT -> "!"
                UnaryOperators.NEG -> "-"
            }

        private fun surround(expr: Expr?) =
            if (expr is BinaryOp)
                "(${printExpr(expr)})"
            else
                printExpr(expr)
    }
}