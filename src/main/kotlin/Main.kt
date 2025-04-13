package org.sdfg610.dims

import org.sdfg610.dims.pretty_printing.PrettyPrinter
import org.sdfg610.dims.static_analysis.AssignAndTypeChecker
import org.sdfg610.dims.static_analysis.EnvAT
import syntactic_analysis.*


fun main(args: Array<String>) {
    // NOTE: Example .dims files are available in the "Examples" folder.
    val fileName: String? = args.getOrNull(0)
    val prettyPrint: Boolean = args.contains("--pretty")

    if (fileName == null)
        println("Usage: Dims [file-name] [--pretty]")
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