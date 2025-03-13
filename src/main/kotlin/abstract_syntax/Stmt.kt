package org.sdfg610.dims.abstract_syntax

/* When matching on a sealed interface in a switch-case, Kotlin will warn you if you forgot to match a possible case */
sealed interface Stmt

data object Skip : Stmt  // Data object: "optimization -> only one instance". Google it if in doubt.

class Comp(var stmt1: Stmt?, var stmt2: Stmt?) : Stmt

class Declaration(var type: Type?, var identifier: Var?) : Stmt

class Assign(var identifier: Var?, var value: Expr?) : Stmt

class Print(var value: Expr?) : Stmt

class If(var condition: Expr?, var thenBody: Stmt?,var elseBody: Stmt?) : Stmt

class While(var condition: Expr?, var body: Stmt?) : Stmt
