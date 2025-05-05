# The Dims language processor

This repository contains a small Kotlin-based implementation of a simple language, Dims, which (at the time of writing) is used as supplementary material for the Languages and Compilers course at Aalborg University. This code has been developed in Jetbrains IntelliJ while using the Coco/R plugin by "Thomas Salzinger" which provides code-highlighting and basic error-checking of Coco/R specifications (i.e., `.ATG`-files). Note that Kotlin is based on Java and that Kotlin can use native Java-code. You need Java version 21 or later to compile the language processor (with the current Gradle-configuration at least).

The implementation includes syntactic analysis, semantic analysis, and interpretation phases, as well as a pretty-printer for Dims programs. A module for compiling Dims programs may come in the future. The lexer (aka. scanner) and parser (the syntactic analysis phase) has been generated using the [Coco/R compiler generator](https://ssw.jku.at/Research/Projects/Coco/), which uses the specification in `CocoR/Dims.ATG` to generate files found in `src/main/java/syntactic_analysis/`. Coco/R can generate code for many different languages such as Java, C#, and C++, to name a few. See also the [Coco/R user manual](https://ssw.jku.at/Research/Projects/Coco/Doc/UserManual.pdf) for all the details about the inner workings thereof. The (linux based) shell-script, `CocoR/cocoR.sh`, can be used to run Coco/R to generate the lexer and parser, but you can also do so manually from a terminal (for Java, see manual for other languages) by running the following command in the `CocoR`-directory:

`java -jar Coco.jar Dims.ATG -package "syntactic_analysis" -o "../src/main/java/syntactic_analysis"`

You can either run the Dims language processor Jar-file from a terminal as shown below, or you can make a run-configuration in Intellij (or whatever IDE you use) and provide program arguments through that. With the `--pretty` argument, the language processor prints a pretty-printed version of the input program, whereas without the argument, the interpreter is run.

`java -jar Dims.jar file-name [--pretty]`

Do note that you have to compile Dims.jar manually (using Gradle) and that this file is not pre-packaged in this repository. To see how to create Dims.jar (using Gradle) through IntelliJ, do refer to steps 7-9 in [this StackOverflow answer](https://stackoverflow.com/a/37102569/2270247).


## Syntactic analysis phase

The parser generated from `CocoR/Dims.ATG` produces an abstract syntax tree (AST) for the parsed program. While Coco/R takes care of the basic string-parsing logic, it is the developer's responsibility to inject code that converts the parse-tree into an AST. The example below shows the specification for parsing assignments (e.g., `i := 5;`), where the `(. .)`-clauses contain AST-generation-code to inject into the generated parser (`t` refers to current token/terminal so, e.g., `t.val` here is the value of the `IDENT` token). The classes used to implement the abstract syntax (and thus used to build ASTs in-memory) are found in `src/main/kotlin/abstract_syntax`.

```
// Pure context-free grammar
Assignment = IDENT ":=" Expr ';'
 
// Attributed grammar with AST-construction logic as seen in "CocoR/Dims.ATG"
Assignment<out Stmt stmt> =
    IDENT                (. String var = t.val; int lineNumber = t.line; .)
    ":="
    Expr<out Expr expr>  (. stmt = new Assign(var, expr, lineNumber); .)
    ';'                           
.
```

Lexers and parsers are not difficult to write by hand, but for beginners (and for simple languages) it is usually best to stick with parser generators.


## Semantic analysis phase
The `src/kotlin/semantic_analysis/AssignAndTypeChecker.kt`-file contains the implementation of the semantic (or static) analysis, which consists of standard type checking as well as checking that variables are declared and assigned to before use. The code in this file is written by hand, as opposed to the parser and lexer.

The implementation uses recursive functions that perform checks based on the syntactic component being checked. Some language implementations uses the "Visitor Pattern", but that is considered outdated and makes the code more complex than it needs to be. 


## Interpretation phase
The `src/kotlin/interpretation/Interpreter.kt`-file implements the actual interpreter component for Dims and uses the same implementation pattern as for the semantic analysis. This code has also been written by hand.


## Pretty printing
The `src/main/kotlin/pretty_printing/PrettyPrinter.Kt`-file contains code for printing an AST as a string. This is a good exercise for figuring out whether your parser works as intended. If the code produced by the pretty-printer has a different meaning from the code you input, something is wrong somewhere. It can be a good exercise to try parsing the pretty-printed code again. You might want to try pretty-printing some of the example Dims-programs from the `Examples`-directory.


## The Dims specification
All the implementation work is based on the Dims specification shown below. Do take a moment to compare the corresponding parts of the implementation and specification.

### Abstract syntax
```
S ∈ Stmt ::= S1;S2 | skip | T x | x := e | print e 
            | if e then S1 else S2 | while e do S .
T ∈ Type ::= int | bool .
e ∈ Expr ::= e1 + e2 | e1 - e2 | e1 * e2 | -e 
            | e1 < e2 | e1 = e2 | e1 || e2 | !e 
            | n | x | true | false .
n ∈ Num
x ∈ Var
```

### Context-free grammar (EBNF)
```
Program  = {Stmt} .

Stmt     = Type IDENT [':=' Expr] ';' 
         | IDENT ':=' Expr ';' 
         | 'print' Expr ';'
         | 'if' '(' Expr ')' 'then' {Stmt} ['else' {Stmt}] 'endif' 
         | 'while' '(' Expr ')' 'do' {Stmt} 'endwhile' .
Type     = 'int' | 'bool'.

Expr     = EqExpr { '||' EqExpr } .
EqExpr   = RelExpr { ('=' | '!=') RelExpr } .
RelExpr  = PlusExpr { ('<') PlusExpr } .
PlusExpr = MultExpr { ('+' | '-') MultExpr } .
MultExpr = NotExpr { '*' NotExpr } .
NotExpr  = {'!' | '-'} Term .
Term     = IDENT | NUM | 'true' | 'false' | '(' Expr ')' .

IDENT  = [_a-zA-Z][_a-zA-Z0-9]*
NUM    = [0-9]+
```


### Static semantics
The type environment `Δ` is a list (or stack) of maps (or partial functions) from variable names to a boolean value, denoting whether the variable has been assigned to yet, and a type, denoting the declared type of the variable. Recall that declarations and assignments are separate in the abstract syntax, which is why "assignment before use" must be checked in addition to "declaration before use". 

The reason for having a stack is to be able to represent nested scopes. We define some helper functions and notation:
- Enter scope: `enter(Δ) = Δ'` where `Δ'` is identical to `Δ` except there has been pushed a new scope on top.
- Leave scope: `leave(Γ) = Γ'` where `Γ'` is identical to `Γ` except the top scope has been removed.
- Set binding: `Δ[x ↦ v] = Δ'` where the first binding (from the top) of `x` is updated to `v`. If no binding of `x` exists, this is considered an error.
- Create binding: `Δ[[x ↦ v]] = Δ'` where the binding of x to v is added to the top-most scope. If a binding of `x` already exists in the top-most scope, this is considered an error.
- Lookup: `Δ[x] = (b, T)` where `(b, T)` comes from the first binding (from the top) of `x`.

We denote ignored values with `_`.

```
Δ ∈ EnvAT = [Var ⇀ (Bool × Type)]

Expression judgments:  Δ ⊢ e : T
Statement judgments:   <S , Δ> : Δ'


// Expressions
           Δ ⊢ e1 : int    Δ ⊢ e2 : int
[ADD_T]   -------------------------------
                 Δ ⊢ e1 + e2 : int
               
           Δ ⊢ e1 : int    Δ ⊢ e2 : int
[SUB_T]   -------------------------------
                 Δ ⊢ e1 - e2 : int
               
           Δ ⊢ e1 : int    Δ ⊢ e2 : int
[MUL_T]   -------------------------------
                 Δ ⊢ e1 * e2 : int
               
           Δ ⊢ e1 : int    Δ ⊢ e2 : int
[LT_T]    -------------------------------
               Δ ⊢ e1 < e2 : bool
               
           Δ ⊢ e1 : T    Δ ⊢ e2 : T
[EQ_T]    ---------------------------
               Δ ⊢ e1 = e2 : bool
               
           Δ ⊢ e1 : bool    Δ ⊢ e2 : bool
[OR_T]    ---------------------------------
                Δ ⊢ e1 || e2 : bool

           Δ ⊢ e : int
[NEG_T]   --------------
           Δ ⊢ -e : int

           Δ ⊢ e : bool
[NOT_T]   ---------------
           Δ ⊢ !e : bool
           
[INT_T]    Δ ⊢ n : int
           
[TRUE_T]   Δ ⊢ true : bool
           
[FALSE_T]  Δ ⊢ false : bool
           
[VAR_T]    Δ ⊢ x : T   where   Δ[x] = (_, T)


// Statements
[SKIP_T]   <skip , Δ> : Δ

            <S1 , Δ> : Δ'    <S2 , Δ'> : Δ''
[COMP_T]   ----------------------------------
                <S1 ; S2 , Δ> : Δ''

[DECL_T]   <T x , Δ> : Δ[[x ↦ T]]

                        Δ ⊢ e : T
[ASS_T]    ---------------------------------    where    Δ[x] = (_, T)
            <x := e , Δ> : Δ[x ↦ (true, T)]

                Δ ⊢ e : _
[PRINT_T]  -------------------
            <print e , Δ> : Δ

            Δ ⊢ e : bool    <S1 , enter(Δ)> : Δ1    <S2 , enter(Δ)> : Δ2
[IF_T]     ---------------------------------------------------------------
                         <if e then S1 else S2 , Δ> : Δ'
           where  Δ' = Δ[x_i](b1_i && b2_i , T_i)  
           and  leave(Δ1)[x_i] = (b1_i , T_i)  and  leave(Δ2)[x_i] = (b2_i , T_i)
           and  x_i ∈ dom(Δ)

            Δ ⊢ e : bool    <S , enter(Δ)> : _
[WHILE_T]  -------------------------------------
                <while e do S , Δ> : Δ
```

The large side-condition in the `[IF_T]`-rule states that `Δ'` only considers a variable "assigned to", if the variable is guaranteed to be assigned to in *both* if-branches. The reason for using `leave` on `Δ1` and `Δ2` is to avoid shadowing declarations from the branch-bodies to have an effect outside the bodies.

Since while-loops are *not* guaranteed to run at least once (e.g., "`while false do ... endwhile`" never runs), no assignments (or declarations due to the loops scope) will "count" after the while-loop. As such, `Δ` remains unchanged at the end of the `[WHILE_T]`-rule.

### Dynamic semantics
The type environment `Γ` is a list of maps from variable names to values or the "unit value" (`•`), the latter of which is used to denote an unassigned variable. Again, the reason for having a stack is to be able to represent nested scopes, and we define the same helper functions and notation as for the type-environment in the previous section.

We use `OUT` to represent the terminal output stream of a program and `OUT += v` represents the operation of printing the string-value of `v` followed by the newline character to the output stream.

```
v ∈ Val = Num ∪ Var
Γ ∈ EnvV = [Var ⇀ Val ∪ •]

Expression judgments:  Γ ⊢ e → v
Statement judgments:   <S , Γ> → Γ'


// Expressions
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[ADD]   ----------------------------
           Γ ⊢ e1 + e2 → (v1 + v2)
               
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[SUB]   ----------------------------
           Γ ⊢ e1 - e2 → (v1 - v2)
               
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[MUL]   ----------------------------
           Γ ⊢ e1 * e2 → (v1 * v2)
               
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[LT]    -----------------------------
           Γ ⊢ e1 < e2 → (v1 < v2)
               
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[EQ]    -----------------------------
           Γ ⊢ e1 = e2 → (v1 = v2)
               
         Γ ⊢ e1 → v1    Γ ⊢ e2 → v2
[OR]    -----------------------------
          Γ ⊢ e1 || e2 → (v1 || v2)

          Γ ⊢ e → v
[NEG]   -------------
         Γ ⊢ -e → -v

          Γ ⊢ e → v
[NOT]   -------------
         Γ ⊢ !e → !v
           
[INT]    Γ ⊢ n → n
           
[TRUE]   Γ ⊢ true → true
           
[FALSE]  Γ ⊢ false → false
           
[VAR]    Γ ⊢ x → Γ[x]


// Statements
[SKIP]      <skip , Γ> → Γ

             <S1 , Γ> → Γ'    <S2 , Γ'> → Γ''
[COMP]      ----------------------------------
                   <S1 ; S2 , Γ> → Γ''

[DECL]      <T x , Γ> → Γ[[x ↦ •]]

                    Γ ⊢ e → v
[ASS]       -------------------------
             <x := e , Γ> → Γ[x ↦ v]

                 Γ ⊢ e → v
[PRINT]     -------------------    where    OUT += v
             <print e , Γ> → Γ

              Γ ⊢ e → true    <S1 , enter(Γ)> → Γ'
[IF_TT]     ----------------------------------------
             <if e then S1 else S2 , Γ> → leave(Γ')

              Γ ⊢ e → false    <S2 , enter(Γ)> → Γ'
[IF_FF]     ----------------------------------------
             <if e then S1 else S2 , Γ> → leave(Γ')

             Γ ⊢ e → true    <S , enter(Γ)> → Γ'    <while e do S , leave(Γ')> → Γ''
[WHILE_TT]  -------------------------------------------------------------------------
                                 <while e do S , Γ> → Γ''

                 Γ ⊢ e → false
[WHILE_FF]  ------------------------
             <while e do S , Γ> → Γ
```

