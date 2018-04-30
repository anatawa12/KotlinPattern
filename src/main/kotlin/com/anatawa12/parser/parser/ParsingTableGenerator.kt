package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.toMapList
import com.anatawa12.libs.util.escape
import com.anatawa12.parser.logging.Log
import com.anatawa12.parser.logging.section
import java.util.*

/**
 * Created by anatawa12 on 2018/02/25.
 */
sealed class Operation{
	class Shift(val to: Int, val syntaxes: List<ClosureItem>) : Operation() {
		override fun toString(): String = "Shift($to)"
	}
	class Reduce(val syntax: Int, val syntaxes: List<ClosureItem>) : Operation() {
		override fun toString(): String = "Reduce($syntax)"
	}
	class Conflicted(val shiftTo: List<Shift>, val reduceSyntax: List<Reduce>) : Operation() {
		override fun toString(): String = "Conflicted($shiftTo)"

		val syntaxes by lazy { shiftTo.flatMap { it.syntaxes } + reduceSyntax.flatMap { it.syntaxes } }
	}
	object Accept : Operation() {
		override fun toString(): String = "Accept"
	}
	class Goto(val to: Int, val syntaxes: List<ClosureItem>) : Operation(){
		override fun toString(): String = "Goto($to)"
	}
}

typealias ParsingTable = Array<Map<Token, Operation>>

class ParsingTableGenerator(val syntax: SyntaxDB, val dfaGenerator: DFAGenerator) {
	var parsingTable: ParsingTable
	init {
		val (success, parsingTable) = generateParsingTable(dfaGenerator.LR1DFA)
		this.parsingTable = parsingTable
		if (!success) {
			val builder = StringBuilder()
			parsingTable.forEachIndexed { id, parsingMap ->
				parsingMap
						.mapNotNull { (key, value) ->
							if (value is Operation.Conflicted)
								return@mapNotNull key to value
							else
								return@mapNotNull null
						}.forEach { (_, op) ->
							//val builder = StringBuilder()
							System.err.print("[warning] ")
							System.err.print(op.shiftTo.joinToString(separator = "/"){"shift"})
							if (op.shiftTo.isNotEmpty() && op.shiftTo.isNotEmpty())
								System.err.print("/")
							System.err.print(op.reduceSyntax.joinToString(separator = "/"){"reduce"})
							System.err.println(" conflicts")
							op.syntaxes
									.map { W(it) }
									.toSet()
									.map { it.value }
									.forEach {
								val syntax = it.syntax[it.syntaxId]
								val ltoken = syntax.ltoken
								val pattern = syntax.pattern.mapTo(mutableListOf()) {
									val result = "^stringLiteral\\$\\$([\\s\\S]*)$".toRegex().find(it.name)
									if (result == null) {
										it.name
									} else {
										"\"${result.groups[1]!!.value}\""
									}
								}
								pattern.add(it.dotIndex, ".")
								System.err.println("	in ClosureItem(${ltoken.name.escape()} -> ${pattern.joinToString(separator = " "){ it.escape() }})")
							}
						}
			}

			System.err.println(builder)

			//throw ConflictedException(builder.toString())
		}
	}

	fun removeConflict(){
		parsingTable.forEachIndexed { index, map ->
			parsingTable[index] = map.mapValues { (token, op) ->
				when (op) {
					is Operation.Conflicted -> {
						op.shiftTo.firstOrNull() ?: op.reduceSyntax.first()
					}
					else -> op
				}
			}
		}
	}

	private class W(val value: ClosureItem) {
		override fun toString(): String {
			return value.toString()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as W

			if (!value.isSameLr0(other.value)) return false

			return true
		}

		override fun hashCode(): Int {
			return value.lr0Hash.hashCode()
		}

	}

	private fun generateParsingTable(dfa: DFA): Pair<Boolean, ParsingTable> = section("generateParsingTable"){
		val parsingTable: ParsingTable = Array(dfa.size){ mutableMapOf<Token, Operation>() }
		var conflicted = false

		dfa.forEachIndexed { i, node ->
			val tmp = mutableListOf<Pair<Token, Operation>>()
			val syntaxes = node.closure.closureSet
			node.edge.mapTo(tmp) { (label, to) ->
				when (label) {
					in syntax.symbols.terminalSymbols -> label to Operation.Shift(to, syntaxes)
					in syntax.symbols.nonterminalSymbols -> label to Operation.Goto(to, syntaxes)
					else -> error("")
				}
			}
			node.closure.closureSet
					.filter { it.dotIndex == syntax[it.syntaxId].pattern.size }
					.flatMapTo(tmp) {
				when {
					it.syntaxId == -1 -> listOf(Token.Eof to Operation.Accept)
					else -> it.lookaheads.map { label ->
						Log.debug("$label $it")
						label to Operation.Reduce(it.syntaxId, syntaxes)
					}
				}
			}
			parsingTable[i] = tmp
					.toMapList()
					.map { (label, ops) ->
						if (ops.size == 1) {
							label to ops.single()
						} else {
							conflicted = true
							label to Operation.Conflicted(
									shiftTo = ops.filterIsInstance<Operation.Shift>(),
									reduceSyntax = ops.filterIsInstance<Operation.Reduce>()
							)
						}
					}
					.toMap()
		}
		return !conflicted to parsingTable
	}

	override fun toString(): String {
		return "ParsingTableGenerator(${Arrays.toString(parsingTable)})"
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