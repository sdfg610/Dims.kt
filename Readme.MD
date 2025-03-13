# The Dims language processor

This repository contains a small Kotlin-based implementation of a simple language, Dims, which (at the time of writing) is used as supplementary material for the Languages and Compilers course at Aalborg University. This code has been developed in Jetbrains IntelliJ while using the Coco/R plugin by "Thomas Salzinger" which provides code-highlighting and basic error-checking of Coco/R specifications (i.e., `.ATG`-files). Note that Kotlin is based on Java and that Kotlin can use native Java-code.

Currently, the Dims language processor implements a lexer, parser, and pretty-printer. Modules for semantic analysis and interpretation will follow at a later point. The lexer and parser has been generated using the [Coco/R compiler generator](https://ssw.jku.at/Research/Projects/Coco/), which uses the specification in `CocoR/Dims.ATG` to generate the Scanner/Lexer and Parser files found in `src/main/java/syntactic_analysis/`. Coco/R can generate code for many different languages such as Java, C#, and C++, to name a few. See also the [Coco/R user manual](https://ssw.jku.at/Research/Projects/Coco/Doc/UserManual.pdf) for all the details about the inner workings. The (linux based) shell-script, `CocoR/cocoR.sh`, can be used to run Coco/R to generate the lexer and parser, but you can also do so manually from a terminal (for Java, see manual) by running the following command in the `CocoR`-directory:

`java -jar Coco.jar Dims.ATG -package "syntactic_analysis" -o "../src/main/java/syntactic_analysis"`

The parser generated from `CocoR/Dims.ATG` produces an abstract syntax tree (AST) for the parsed program. While Coco/R takes care of the basic string-parsing logic, it is the developer's responsibility to input code that converts the parse-tree into an AST. The example below shows the specification for parsing assignments (e.g., `i := 5;`), where the `(. .)`-clauses contain AST-generation-code to inject into the generated parser (`t` refers to current token/terminal, so `t.val` here is the name of an identifier). The classes used to implement the abstract syntax (and thus used to build ASTs in-memory) are found in `src/main/kotlin/abstract_syntax`.

```
// Pure grammar
Assignment = IDENT ":=" Expr ';'
 
// Grammar with AST-construction logic as seen in "CocoR/Dims.ATG"
Assignment<out Stmt stmt> =
    IDENT                    (. Var var = new Var(t.val); .)
    ":="
    Expr<out Expr expr>      (. stmt = new Assign(var, expr); .)
    ';'                           
.
```

The `src/main/kotlin/pretty_printing/PrettyPrinter.Kt`-file contains code for printing an AST as a string. This is a good exercise for figuring out whether your parser works as intended. If the code produced by the pretty-printer is different from the code you input, something is wrong somewhere. You might want to try pretty-printing some of the example Dims-programs from the `Examples`-directory.

The `src/main/kotlin/Main.Kt`-file explains how to use the Dims language processor.

## Dims specification

### Abstract syntax of Dims
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

### Context-free grammar of Dims
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


