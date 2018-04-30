package com.anatawa12.parser.parser

import com.anatawa12.libs.util.escape

/**
 * Created by anatawa12 on 2018/04/15.
 */
data class SyntaxDefinitionSection constructor(val ltoken: Token, val pattern: List<Token>) {
	override fun toString(): String {
		return "${ltoken.name.escape()} -> ${pattern.joinToString(separator = " "){"\"${it.name.escape()}\""}}"
	}
}

typealias SyntaxDefinitions = List<SyntaxDefinitionSection>

