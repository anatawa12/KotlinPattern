package com.anatawa12.parser1.parser

import com.anatawa12.libs.collections.InitMap
import com.anatawa12.parser1.frontend.ast.PatternElement
import com.anatawa12.parser1.frontend.ast.TokenElement
import kotlin.properties.Delegates

/**
 * Created by anatawa12 on 2018/04/15.
 */
sealed class Token {
	abstract val name: String

	val kindData by lazy { kindDatas[this] }

	var resultId
		get() = try { kindData.resultId } catch (e: IllegalStateException) { throw IllegalStateException("no ResultId for $name", e) }
		set(value) { kindData.resultId = value }

	var runTimeId
		get() = kindData.runTimeId
		set(value) { kindData.runTimeId = value }

	var viewName
		get() = kindData.viewName
		set(value) { kindData.viewName = value }

	var isIgnore: Boolean = false

	abstract val type: TokenType

	init {
		viewNameMap[name]?.also { viewName = it }
	}

	companion object {
		private val kindDatas = InitMap<Token, TokenKindData> { TokenKindData(it) }
		val viewNameMap = mutableMapOf<String, String>()
		val Eof = EofToken
		val Syntax = SyntaxToken
		private val _terminalTokenNames: MutableSet<String> = mutableSetOf()
		fun addTerminalName(name: String) {
			_terminalTokenNames.add(name)
		}

		fun isTerminalName(name: String) = name in _terminalTokenNames
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Token

		if (name != other.name) return false
		if (type != other.type) return false

		return true
	}

	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + type.hashCode()
		return result
	}
}

object EofToken: Token() {
	override val name: String get() = "$"
	override val type: TokenType get() = TokenType.Terminal
}

object SyntaxToken: Token() {
	override val name: String get() = "'S"
	override val type: TokenType get() = TokenType.NonTerminal
}

class ZeroMoreToken(elem: PatternElement): Token() {
	override val name: String = "${elem.name}*"
	override val type: TokenType = TokenType.NonTerminal
}

class OneMoreToken(elem: PatternElement): Token() {
	override val name: String = "${elem.name}+"
	override val type: TokenType = TokenType.NonTerminal
}

class OptionalToken(elem: PatternElement): Token() {
	override val name: String = "${elem.name}?"
	override val type: TokenType = TokenType.NonTerminal
}

class WithSepToken(elem: PatternElement, sep: PatternElement): Token() {
	override val name: String = "${elem.name}&${sep.name}"
	override val type: TokenType = TokenType.NonTerminal
}

class StringToken(val value: String): Token() {
	init {
		viewNameMap[value]?.also { viewName = it }
	}

	override val name: String
		get() = "\"$value\""
	override val type: TokenType
		get() = TokenType.Terminal
}

class NamedToken(val baseElement: TokenElement, override val type: TokenType): Token() {
	override val name: String = baseElement.name
}

class TokenKindData(it: Token) {
	var resultId: Int by Delegates.notNull()
	var runTimeId: Int by Delegates.notNull()
	var viewName: String = it.name
}






/*
open class Token constructor(val value: String, val data: String? = null, private val _viewName: String? = null){

	var runTimeId: Int by Delegates.notNull()
	var resultId: Int by Delegates.notNull()
	lateinit var type: TokenType

	fun setIdAndTypeFromMap() {
		map[this]?.also { (id, type) ->
			resultId = id
			runTimeId = id + 1
			this.type = type
		}
	}

	init {
		setIdAndTypeFromMap()
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Token) return false
		if(this === Eof || this === Syntax)
			return this === other
		return value == other.value
	}

	override fun hashCode(): Int {
		return value.hashCode()
	}

	override fun toString(): String = if (data == null) "Token(${value.escape()})" else "Token(${value.escape()}, \"${data.escape()}\")"

	val viewName get() = _viewName ?: stringNameMap[value] ?: value

	companion object {
		var map = mapOf<Token, Pair<Int, TokenType>>().synchronized()
		val stringNameMap = mutableMapOf<String, String>()
		val Eof = Token("EOF")
		val Syntax = Token("S'")
		private fun toTokenName(string: String) = "\"$string\""

		private fun toString1(x: PatternElement): String = when (x) {
			is TokenElement -> stringNameMap[x.value]?.also { println("${x.value} to $it in token") } ?: x.value
			is StringElement -> stringNameMap[x.value]?.also { println("${x.value} to $it in string") } ?: toTokenName(x.value)
		}

		private fun toString1(x: String): String = stringNameMap[x]?.also { println("$x to $it") } ?: x

		private fun toString2(x: PatternElement): String = when (x) {
			is TokenElement -> x.value
			is StringElement -> toTokenName(x.value)
		}

		private val PatternElement.value get() = when(this) {
			is TokenElement -> value
			is StringElement -> value
		}

		fun fromPatternExp(element: PatternExp): Token {
			val value = when (element) {
				is PatternElementExp -> toString2(element.elem)
				is PatternElementZeroMoreExp -> toString2(element.elem) + "*"
				is PatternElementOneMoreExp -> toString2(element.elem) + "+"
				is PatternElementOptionalExp -> toString2(element.elem) + "?"
				is PatternElementWithSepExp -> "${toString2(element.elem)}&${toString2(element.sep)}"
			}
			val viewName = when (element) {
				is PatternElementExp -> toString1(element.elem)
				is PatternElementZeroMoreExp -> toString1(toString2(element.elem) + "*")
				is PatternElementOneMoreExp -> toString1(toString2(element.elem) + "+")
				is PatternElementOptionalExp -> toString1(toString2(element.elem) + "?")
				is PatternElementWithSepExp -> toString1("${toString2(element.elem)}&${toString2(element.sep)}")
			}
			stringNameMap[value] = viewName
			return Token(value, _viewName = viewName)
		}

		fun fromPatternElement(element: PatternElement): Token {
			val value = toString2(element)
			val viewName = toString1(element)
			stringNameMap[value] = viewName
			return Token(value, _viewName = viewName)
		}

		fun leftToken(pattern: Pattern): Token = Token(pattern.lToken)
	}
}
// */

