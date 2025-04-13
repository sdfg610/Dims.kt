package org.sdfg610.dims

import org.sdfg610.dims.abstract_syntax.*
import org.sdfg610.dims.pretty_printing.PrettyPrinter
import syntactic_analysis.*


fun main(args: Array<String>) {
    // NOTE: Example .dims files are available in the "Examples" folder.
    val fileName: String? = args.getOrNull(0)
    val prettyPrint: Boolean = args.contains("--pretty")

    if (fileName != null) {
        try {
            val parser = Parser(Scanner(fileName))

            parser.Parse()
            val program: Stmt = parser.mainNode
            if (parser.hasErrors())
                println("Errors during parsing!")
            else if (prettyPrint)
                println(PrettyPrinter.printStmt(program))
            else {
                println("Semantic analysis and interpretation will follow later")
                // TODO: Semantic analysis and interpretation.
            }
        }
        catch (ex: Exception) {
            println("Error during parsing:")
            println(ex.toString())
        }
    }
    else
        println("Usage: Dims [file-name] [--pretty] [--interpret]")
}