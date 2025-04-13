package org.sdfg610.dims.static_analysis

import org.sdfg610.dims.abstract_syntax.*
import org.sdfg610.dims.common.Env
import org.sdfg610.dims.pretty_printing.PrettyPrinter

typealias EnvAT = Env<AT>

data class AT(var isAssigned: Boolean, val type: Type)

class AssignAndTypeChecker {
    val errors: MutableList<String> = mutableListOf()

    val hasErrors get() = errors.isNotEmpty()


    constructor(stmt: Stmt, envAT: EnvAT) {
        stmtT(stmt, envAT)
    }

    constructor(expr: Expr, envAT: EnvAT) {
        exprT(expr, envAT)
    }


    private fun stmtT(stmt: Stmt, envAT: EnvAT): Unit {
        when (stmt) {
            Skip -> { /* Nothing to do here */ }

            is Print -> exprT(stmt.value!!, envAT) // Anything is fine as long as the value-expression is well-formed.

            is Assign -> {
                val exprType = exprT(stmt.value!!, envAT)  // "!!"  ==>  "trust me bro, it is not null". Will never be null when parsing has no errors.
                val at = envAT.tryGet(stmt.identifier!!)
                at?.isAssigned = true

                if (at == null)
                    errors.add("Line ${stmt.lineNumber}: Assignment to undeclared variable '${stmt.identifier!!}'")
                else if (exprType != null && exprType.javaClass != at.type.javaClass)
                    errors.add("Line ${stmt.lineNumber}: Assignment has variable with type '${PrettyPrinter.printType(at.type)}' but expression with type '${PrettyPrinter.printType(exprType)}'.")
            }

            is Comp -> {
                stmtT(stmt.stmt1!!, envAT)
                stmtT(stmt.stmt2!!, envAT)
            }

            is Declaration -> {
                if (envAT.isLocal(stmt.identifier!!))
                    errors.add("Line ${stmt.lineNumber}: Redeclaration of variable '${stmt.identifier}' is same scope.")
                else
                    envAT.bind(stmt.identifier!!, AT(false, stmt.type!!)) // Declared variable is not assigned by default, thus 'false'.
            }

            is If -> {
                val exprType = exprT(stmt.condition!!, envAT)
                if (exprType != null && exprType !is BoolT)
                    errors.add("Line ${stmt.condition!!.lineNumber}: If statement requires a condition with type 'bool' but got '${PrettyPrinter.printType(exprType)}'.")

                stmtT(stmt.thenBody!!, envAT.newScope()) // The 'then'- and 'else'-bodies gets their own scopes.
                stmtT(stmt.elseBody!!, envAT.newScope())
            }

            is While -> {
                val exprType = exprT(stmt.condition!!, envAT)
                if (exprType != null && exprType !is BoolT)
                    errors.add("Line ${stmt.condition!!.lineNumber}: While statement requires a condition with type 'bool' but got '${PrettyPrinter.printType(exprType)}'.")

                stmtT(stmt.body!!, envAT.newScope()) // The body gets its own scope.
            }
        }
    }

    private fun exprT(expr: Expr, envAT: EnvAT): Type? { // Returns "nullable Type" since we cannot type un-bound variables
        return when (expr) {
            is BoolV -> BoolT
            is NumV -> IntT
            is Ref -> {
                val at = envAT.tryGet(expr.name)

                if (at == null)
                    errors.add("Line ${expr.lineNumber}: Use of declared variable '${expr.name}'.")
                else if (!at.isAssigned)
                    errors.add("Line ${expr.lineNumber}: Use of unassigned variable '${expr.name}'.")

                return at?.type
            }

            is BinaryOp -> {
                val typeL = exprT(expr.exprLeft, envAT)
                val typeR = exprT(expr.exprRight, envAT)

                when (expr.op) {
                    BinaryOperators.ADD,
                    BinaryOperators.SUB,
                    BinaryOperators.MUL,
                    BinaryOperators.LT -> {
                        if (typeL != null && typeL !is IntT)
                            errors.add("Line ${expr.exprLeft.lineNumber}: Operator '${PrettyPrinter.binaryOpString(expr.op)}' expected a left operand of type 'int', but got '${PrettyPrinter.printType(typeL)}'.")
                        if (typeR != null && typeR !is IntT)
                            errors.add("Line ${expr.exprRight.lineNumber}: Operator '${PrettyPrinter.binaryOpString(expr.op)}' expected a right operand of type 'int', but got '${PrettyPrinter.printType(typeL)}'.")
                    }
                    BinaryOperators.OR -> {
                        if (typeL != null && typeL !is BoolT)
                            errors.add("Line ${expr.exprLeft.lineNumber}: Operator '${PrettyPrinter.binaryOpString(expr.op)}' expected a left operand of type 'bool', but got '${PrettyPrinter.printType(typeL)}'.")
                        if (typeR != null && typeR !is BoolT)
                            errors.add("Line ${expr.exprRight.lineNumber}: Operator '${PrettyPrinter.binaryOpString(expr.op)}' expected a right operand of type 'bool', but got '${PrettyPrinter.printType(typeL)}'.")
                    }
                    BinaryOperators.EQ -> {
                        if (typeL != null && typeR != null && typeL.javaClass != typeR.javaClass)
                            errors.add("Line ${expr.lineNumber}: Operator '${PrettyPrinter.binaryOpString(expr.op)}' expected operands of the same type, but got '${PrettyPrinter.printType(typeL)}' and '${PrettyPrinter.printType(typeR)}'.")
                    }
                }

                return when (expr.op) {
                    BinaryOperators.ADD, BinaryOperators.SUB, BinaryOperators.MUL -> IntT
                    BinaryOperators.LT, BinaryOperators.EQ, BinaryOperators.OR -> BoolT
                }
            }

            is UnaryOp -> {
                val type = exprT(expr.expr, envAT)

                when (expr.op) {
                    UnaryOperators.NOT -> {
                        if (type != null && type !is BoolT)
                            errors.add("Line ${expr.lineNumber}: Operator '${PrettyPrinter.unaryOpString(expr.op)}' expected an operand of type 'bool', but got '${PrettyPrinter.printType(type)}'.")
                    }
                    UnaryOperators.NEG -> {
                        if (type != null && type !is IntT)
                            errors.add("Line ${expr.lineNumber}: Operator '${PrettyPrinter.unaryOpString(expr.op)}' expected an operand of type 'int', but got '${PrettyPrinter.printType(type)}'.")
                    }
                }

                return when (expr.op) {
                    UnaryOperators.NOT -> BoolT
                    UnaryOperators.NEG -> IntT
                }
            }
        }
    }
}