package org.sdfg610.dims.interpretation

import org.sdfg610.dims.abstract_syntax.*
import org.sdfg610.dims.abstract_syntax.BoolV
import org.sdfg610.dims.common.Env

typealias EnvV = Env<Val?>


class Interpreter {
    companion object {
        fun evalStmt(stmt: Stmt, envV: EnvV): Unit {
            when (stmt) {
                Skip -> { /* Nothing to do here */ }
                is Print -> {
                    val value = evalExpr(stmt.value!!, envV)
                    println(value.toString())
                }

                is Assign -> {
                    val value = evalExpr(stmt.value!!, envV)
                    envV.set(stmt.identifier!!, value)
                }

                is Comp -> {
                    evalStmt(stmt.stmt1!!, envV)
                    evalStmt(stmt.stmt2!!, envV)
                }

                is Declaration -> envV.bind(stmt.identifier!!, null)

                is If -> {
                    val condition = evalExpr(stmt.condition!!, envV)
                    if (condition.asBool())
                        evalStmt(stmt.thenBody!!, envV.newScope())
                    else
                        evalStmt(stmt.elseBody!!, envV.newScope())
                }

                is While -> {
                    // Note: "var" means "can change", "val" means "read-only"
                    var condition = evalExpr(stmt.condition!!, envV)
                    while (condition.asBool()) {
                        evalStmt(stmt.body!!, envV)
                        condition = evalExpr(stmt.condition!!, envV)
                    }
                }
            }
        }

        fun evalExpr(expr: Expr, envV: EnvV): Val {
            return when (expr) {
                is BoolV -> BoolVal(expr.value)
                is NumV -> IntVal(expr.value)
                is Ref -> envV.tryGet(expr.name)!! // The static analysis ensures this value is never null

                is BinaryOp -> {
                    val v1 = evalExpr(expr.exprLeft, envV)
                    val v2 = evalExpr(expr.exprRight, envV)

                    return when (expr.op) {
                        BinaryOperators.ADD -> IntVal(v1.asInt() + v2.asInt())
                        BinaryOperators.SUB -> IntVal(v1.asInt() - v2.asInt())
                        BinaryOperators.MUL -> IntVal(v1.asInt() * v2.asInt())
                        BinaryOperators.LT -> BoolVal(v1.asInt() < v2.asInt())
                        BinaryOperators.EQ -> BoolVal(v1 == v2) // IntVal and BoolVal are "data classes" and auto-generate "equals" based on member-values.
                        BinaryOperators.OR -> BoolVal(v1.asBool() || v2.asBool())
                    }
                }

                is UnaryOp -> when (expr.op) {
                    UnaryOperators.NOT -> BoolVal(!(evalExpr(expr.expr, envV).asBool()))
                    UnaryOperators.NEG -> IntVal(-(evalExpr(expr.expr, envV).asInt()))
                }
            }
        }
    }
}