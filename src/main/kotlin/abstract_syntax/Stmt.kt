package org.sdfg610.dims.abstract_syntax

/* When matching on a sealed interface in a switch-case, Kotlin will warn you if you forgot to match a possible case */
sealed interface Stmt {
    val lineNumber: Int
}

data object Skip : Stmt {  // Data object: "optimization -> only one instance". Google it if in doubt.
    override val lineNumber: Int get() = throw Exception("Skip does not have a line number")
}

class Comp(var stmt1: Stmt?, var stmt2: Stmt?) : Stmt {
    override val lineNumber: Int
        get() = stmt1?.lineNumber
            ?: throw Exception("Left statement of ';' is 'null'. Cannot get line number")
}

class Declaration(var type: Type?, var identifier: Var?, override val lineNumber: Int) : Stmt

class Assign(var identifier: Var?, var value: Expr?, override val lineNumber: Int) : Stmt

class Print(var value: Expr?, override val lineNumber: Int) : Stmt

class If(var condition: Expr?, var thenBody: Stmt?, var elseBody: Stmt?, override val lineNumber: Int) : Stmt

class While(var condition: Expr?, var body: Stmt?, override val lineNumber: Int) : Stmt
