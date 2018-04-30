package com.anatawa12.parser.frontend

import com.anatawa12.parser.parser.*
import java.util.*

/**
 * Created by anatawa12 on 2018/04/14.
 */
class SyntaxRunnner(generator: ParsingTableGenerator, private val _lexer: ()-> Token, private val skipableTokens: Set<Token>, private val ast: (Token, List<Any?>)->Any?, private val errorAt: ()->Pair<Int, Int>) {
	private val table: ParsingTable = generator.parsingTable
	private val syntaxDB = generator.syntax
	private val stack: LinkedList<Pair<Pair<Int, List<ClosureItem>>, Any?>> = LinkedList()
	private val _result = mutableListOf<Int>()
	var result: Any? = null
	val resultSyntaxes : List<Int> get() = _result
	private var next: Token? = null
	private fun lexer(): Token {
		if (next == null) next = _lexer()
		return next!!
	}

	private fun read() {next = null}

	init {
		stack.push(0 to listOf(ClosureItem(syntaxDB, -1, 0, mutableListOf(Token.Eof))) to null)
	}

	fun runAInsn (): Boolean{
		val map = table[stack.peek().first.first]
		var insn = map[lexer()]
		val items: List<ClosureItem> = when (insn) {
			is Operation.Shift -> insn.syntaxes
			is Operation.Reduce -> insn.syntaxes
			is Operation.Conflicted -> insn.reduceSyntax.flatMap { it.syntaxes } + insn.shiftTo.flatMap { it.syntaxes }
			Operation.Accept -> stack.peek().first.second
			is Operation.Goto -> stack.peek().first.second
			null -> stack.peek().first.second
		}
		do {
			var doNext = false
			when (insn) {
				is Operation.Shift -> {
					stack.push(insn.to to insn.syntaxes to lexer())
					read()
				}
				is Operation.Reduce -> {
					val syntaxId = insn.syntax
					val syntax = syntaxDB[syntaxId]
					_result.add(syntaxId)
					val tokens = mutableListOf<Any?>()
					repeat(syntax.pattern.size) { tokens.add(stack.pop().second) }
					tokens.reverse()
					val t = ast(syntax.ltoken, tokens)
					stack.push((table[stack.peek().first.first][syntax.ltoken] as? Operation.Goto ?: error("")).to to insn.syntaxes to t)
				}
				is Operation.Conflicted -> {
					if (insn.shiftTo.isNotEmpty()) {
						insn = insn.shiftTo[0]
					} else {
						insn = insn.reduceSyntax[0]
					}
					doNext = true
				}
				Operation.Accept -> {
					result = stack.pop().second
					return true
				}
				is Operation.Goto -> {
					error("can't do Goto: $insn, ${lexer()}")
				}
				null -> {
					if (lexer() !in skipableTokens) {
						error("invalid token: ${lexer()} \n" +
								"\tmap: $map\n" +
								"${map.values.mapNotNull { when(it){
									is Operation.Shift -> it.syntaxes
									is Operation.Reduce -> it.syntaxes
									is Operation.Conflicted -> it.syntaxes
									else -> null
								}}.flatMap { it }.joinToString(separator = "\n") { "\tin $it" }}\n" +
								"${stack.peek().first.second.joinToString(separator = "\n") { "\tafter $it" }}\n" +
								"\tat ${errorAt()}\n" +
								"\tafter ${resultSyntaxes.map { syntaxDB[it] }}")
					}
					doNext = true
					read()
					insn = map[lexer()]

				}
			}
		} while (doNext)
		return false
	}

	fun run() {
		while (!runAInsn());
	}
}
