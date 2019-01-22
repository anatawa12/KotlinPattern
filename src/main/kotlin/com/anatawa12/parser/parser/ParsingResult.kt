package com.anatawa12.parser1.parser

import java.util.*
import kotlin.properties.Delegates

/**
 * Created by anatawa12 on 2018/02/25.
 */
typealias ParsingTable = Array<out Map<Token, Operation>>

class ParsingResult(var parsingTable: ParsingTable, val tokenSet: Set<Token>, val syntaxes: SyntaxDefinitions) {

	override fun toString(): String {
		return "ParsingResult(${Arrays.toString(parsingTable)})"
	}
}

data class GrammarDefinition(
		val syntax: SyntaxDefinitions,
		val start_symbol: Token)

class ConflictedException : Exception{
	constructor() : super()
	constructor(message: String) : super(message)
	constructor(message: String, cause: Throwable) : super(message, cause)
	constructor(cause: Throwable) : super(cause)
}

sealed class Operation{
	class Shift(val to: Int, val syntaxes: List<ClosureItem>) : Operation() {
		override fun toString(): String = "Shift($to)"
	}
	class Goto(val to: Int, val syntaxes: List<ClosureItem>) : Operation() {
		override fun toString(): String = "Goto($to)"
	}
	class Reduce(val syntax: Int, val syntaxes: List<ClosureItem>) : Operation() {
		override fun toString(): String = "Reduce($syntax)"
	}
	class Conflicted(val shiftTo: MutableList<Shift>, val reduceSyntax: MutableList<Reduce>) : Operation() {
		override fun toString(): String = "Conflicted($shiftTo)"

		val syntaxes by lazy { shiftTo.flatMap { it.syntaxes } + reduceSyntax.flatMap { it.syntaxes } }
	}
	object Accept : Operation() {
		override fun toString(): String = "Accept"
	}
}

data class SyntaxDefinitionSection(val ltoken: Token, val pattern: List<Token>, val fromFileName: String? = null) {
	var id: Int by Delegates.notNull()

	private val _toString: String by lazy {
		val builder = StringBuilder("SyntaxDefinitionSection(")
		builder.append(ltoken.name.toWritable())
		builder.append(" -> ")
		pattern.joinTo(builder, prefix = "(", postfix = ")", separator = " "){ it.name.toWritable() }
		builder.toString()
	}
	override fun toString() = _toString
}

typealias SyntaxDefinitions = List<SyntaxDefinitionSection>

class ReduceReduceConflictException(): Exception()
