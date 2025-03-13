#!/bin/bash

java -jar Coco.jar Dims.ATG -package "syntactic_analysis" -o "../src/main/java/syntactic_analysis"

[ -e "../src/main/java/syntactic_analysis/Parser.java.old" ] && rm "../src/main/java/syntactic_analysis/Parser.java.old"

[ -e "../src/main/java/syntactic_analysis/Scanner.java.old" ] && rm "../src/main/java/syntactic_analysis/Scanner.java.old"
