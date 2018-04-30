package com.anatawa12.parser.parser

/**
 * Created by anatawa12 on 2018/04/22.
 */
fun GenerateParsingTable(grammar: GrammarDefinition): ParsingTableGenerator {
	val syntax = SyntaxDB(grammar)
	val dfaGenerator = DFAGenerator(syntax)
	return ParsingTableGenerator(syntax, dfaGenerator)
}
