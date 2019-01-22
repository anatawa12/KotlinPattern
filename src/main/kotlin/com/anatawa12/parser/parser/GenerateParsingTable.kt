package com.anatawa12.parser1.parser

import com.anatawa12.parser1.frontend.KotlinPatternArguments

/**
 * Created by anatawa12 on 2018/04/22.
 */
@Throws(ReduceReduceConflictException::class)
suspend fun GenerateParsingTable(grammar: GrammarDefinition, args: KotlinPatternArguments): ParsingResult {
	return generates(grammar, args)
}
