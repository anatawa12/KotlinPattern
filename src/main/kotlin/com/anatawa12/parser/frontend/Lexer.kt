package com.anatawa12.parser.frontend

import com.anatawa12.libs.util.escape
import com.anatawa12.parser.frontend.generated.Token
import com.anatawa12.parser.frontend.generated.TokenType
import kotlin.coroutines.experimental.buildSequence

/**
 * Created by anatawa12 on 2018/04/15.
 */
class Lexer(val input: CharSequence) {

	private val itr = buildSequence<Token> {
		var line = 0
		var column = 0
		var tokenLine = 0
		var tokenColumn = 0
		var stringBuilder = StringBuilder()
		var state: State = State.Ready
		for (c in input) {
			column++
			if (c == '\n') {
				line++
				column = 0
			}
			do {
				var isW = false
				when (state) {
					State.Ready -> {
						when (c) {
							'(' -> yield(Token(TokenType.`"("`, "(", line, column))
							')' -> yield(Token(TokenType.`")"`, ")", line, column))
							'{' -> yield(Token(TokenType.`"{"`, "{", line, column))
							'}' -> yield(Token(TokenType.`"}"`, "}", line, column))
							'=' -> yield(Token(TokenType.`"="`, "=", line, column))
							';' -> yield(Token(TokenType.`"!s"`, ";", line, column))
							':' -> yield(Token(TokenType.`"!c"`, ":", line, column))
							'*' -> yield(Token(TokenType.`"*"`, "*", line, column))
							'+' -> yield(Token(TokenType.`"+"`, "+", line, column))
							'?' -> yield(Token(TokenType.`"?"`, "?", line, column))
							'|' -> yield(Token(TokenType.`"|"`, "|", line, column))
							'.' -> yield(Token(TokenType.`"!d"`, ".", line, column))
							',' -> yield(Token(TokenType.`","`, ",", line, column))
							'<' -> yield(Token(TokenType.`"!la"`, "<", line, column))
							'>' -> yield(Token(TokenType.`"!ra"`, ">", line, column))
							'\n' -> yield(Token(TokenType.LF, "\n", line, column))
							'\r' -> yield(Token(TokenType.LF, "\r", line, column))
							'"' -> {
								tokenLine = line
								tokenColumn = column
								state = State.String
							}
							'@' -> {
								stringBuilder.append(c)
								state = State.AtIdentifier
							}
							else -> {
								if (c.isLetter() || c == '_') {// kotlin identifier start
									tokenLine = line
									tokenColumn = column
									stringBuilder.append(c)
									state = State.Identifier
								} else if (c.isWhitespace()) {
								} else error("invalid char :'${c.toString().escape()}'at line: $line column: $column")
							}
						}
					}
					State.Identifier -> {
						if (c.isLetterOrDigit() || c == '_') {
							stringBuilder.append(c)
						} else {
							val value = stringBuilder.toString()
							when (value) {
								"as" -> yield(Token(TokenType.`"as"`, value, tokenLine, tokenColumn))
								"in" -> yield(Token(TokenType.`"in"`, value, tokenLine, tokenColumn))
								"out" -> yield(Token(TokenType.`"out"`, value, tokenLine, tokenColumn))
								else -> yield(Token(TokenType.SimpleName, value, tokenLine, tokenColumn))
							}
							stringBuilder = StringBuilder()
							isW = true
							state = State.Ready
						}
					}
					State.String -> {
						when (c) {
							'"' -> {
								val value = stringBuilder.toString()
								yield(Token(TokenType.string, value, tokenLine, tokenColumn))
								stringBuilder = StringBuilder()
								state = State.Ready
							}
							'\\' -> state = State.StringEscape
							else -> stringBuilder.append(c)
						}
					}
					State.StringEscape -> {
						when (c) {
							'u' -> {
							}
							't' -> stringBuilder.append('\t')
							'b' -> stringBuilder.append('\b')
							'n' -> stringBuilder.append('\n')
							'r' -> stringBuilder.append('\r')
							'\'' -> stringBuilder.append('\'')
							'"' -> stringBuilder.append('\"')
							'\\' -> stringBuilder.append('\\')
							'$' -> stringBuilder.append('\$')
							else -> error("invalid escape : '${c.toString().escape()}' at line: $line column: $column")
						}
						state = if (c == 'u') State.StringUnicoeEscape(0, 0) else State.String
					}
					is State.StringUnicoeEscape -> {
						state.count++;
						state.value *= 16
						when (c) {
							in '0'..'9' -> state.value += c.toInt() - '0'.toInt()
							in 'A'..'F' -> state.value += c.toInt() - 'A'.toInt() + 10
							in 'a'..'f' -> state.value += c.toInt() - 'A'.toInt() + 10
							else -> error("invalid hex : '${c.toString().escape()}' at line: $line column: $column")
						}
						if (state.count == 4) {
							stringBuilder.append(state.value)
							state = State.String
						}
					}
					State.AtIdentifier -> {
						if (c.isLetterOrDigit() || c == '_') {
							stringBuilder.append(c)
						} else {
							val value = stringBuilder.toString()
							when (value) {
								"@package" -> yield(Token(TokenType.`"@package"`, value, tokenLine, tokenColumn))
								"@import" -> yield(Token(TokenType.`"@import"`, value, tokenLine, tokenColumn))
								"@skip" -> yield(Token(TokenType.`"@skip"`, value, tokenLine, tokenColumn))
								else -> error("invalid token : ${value.escape()} at line: $line column: $column")
							}
							stringBuilder = StringBuilder()
							isW = true
							state = State.Ready
						}
					}
				}
			} while (isW)
		}
	}.iterator()

	fun lex(): Token = if (itr.hasNext()) itr.next()
	else Token(TokenType.EOF, "", -1, -1)


	private sealed class State {
		object Ready : State()
		object Identifier : State()
		object String : State()
		object StringEscape : State()
		class StringUnicoeEscape(var count: Int, var value: Int) : State()
		object AtIdentifier : State()
	}
}
