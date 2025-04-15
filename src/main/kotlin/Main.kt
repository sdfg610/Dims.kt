package org.sdfg610.dims

import org.sdfg610.dims.interpretation.*
import org.sdfg610.dims.pretty_printing.PrettyPrinter
import org.sdfg610.dims.semantic_analysis.*
import syntactic_analysis.*


// TODO: Make error recovery annotations in Dims.ATG for more stable syntax-error-reporting

fun main(args: Array<String>) {
    // NOTE: Example .dims files are available in the "Examples" folder.
    val fileName: String? = args.getOrNull(0)
    val prettyPrint: Boolean = args.contains("--pretty")

    if (fileName == null)
        println("Usage: Dims file-name [--pretty]")
    else {
        try {
            val parser = Parser(Scanner(fileName))
            parser.Parse()

            if (parser.hasErrors())
                println("Errors during syntactic analysis!")
            else if (prettyPrint)
                println(PrettyPrinter.printStmt(parser.mainNode!!))
            else {
                val program = parser.mainNode!!
                val atChecker = AssignAndTypeChecker(program, EnvAT())

                if (atChecker.hasErrors) {
                    for (err in atChecker.errors)
                        println(err)
                    println("Errors during static analysis!")
                }
                else {
                    println("Running program!")
                    Interpreter.evalStmt(program, EnvV())
                    println("Program terminated!")
                }
            }
        }
        catch (ex: Exception) {
            println("An exception was thrown:")
            ex.printStackTrace()
        }
    }
}