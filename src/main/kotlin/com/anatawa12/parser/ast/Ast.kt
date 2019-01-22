package com.anatawa12.parser.frontend.ast

import com.anatawa12.parser.parser.*

/**
 * Created by anatawa12 on 2018/04/17.
 */

data class Kpt(val tops: List<TopLevelObject>)

sealed class TopLevelObject(){
	lateinit var fromFile: String
}

data class Package(val packageName: String) : TopLevelObject()

data class ImportFile(val from: String) : TopLevelObject()

data class ImportPackeage(val packageName: String) : TopLevelObject()

data class Skip(val tokens: List<String>) : TopLevelObject()

data class Pattern(val type: String, val lToken: String, val pattern: List<PatternElements>) : TopLevelObject() {
	val lTokenToken by lazy { NamedToken(TokenElement(lToken), TokenType.NonTerminal) }
}

data class StringName(val string: String, val name: String) : TopLevelObject()

typealias PatternElements = List<PatternExp>

sealed class PatternExp() {
	abstract val isIgnore: Boolean
	abstract fun toToken(): Token
}

data class PatternElementExp(val elem: PatternElement) : PatternExp() {
	constructor(elem: PatternElement, isIgnore: Boolean): this(elem) {
		this.isIgnore = isIgnore
	}
	override var isIgnore: Boolean = false
		private set

	override fun toToken() = elem.toToken().also { it.isIgnore = isIgnore }
}

data class PatternElementZeroMoreExp(val elem: PatternElement) : PatternExp() {
	constructor(elem: PatternElement, isIgnore: Boolean): this(elem) {
		this.isIgnore = isIgnore
	}
	override var isIgnore: Boolean = false
		private set

	override fun toToken() = ZeroMoreToken(elem).also { it.isIgnore = isIgnore }
}

data class PatternElementOneMoreExp(val elem: PatternElement) : PatternExp() {
	constructor(elem: PatternElement, isIgnore: Boolean): this(elem) {
		this.isIgnore = isIgnore
	}
	override var isIgnore: Boolean = false
		private set

	override fun toToken() = OneMoreToken(elem).also { it.isIgnore = isIgnore }
}

data class PatternElementOptionalExp(val elem: PatternElement) : PatternExp() {
	constructor(elem: PatternElement, isIgnore: Boolean): this(elem) {
		this.isIgnore = isIgnore
	}
	override var isIgnore: Boolean = false
		private set

	override fun toToken() = OptionalToken(elem).also { it.isIgnore = isIgnore }
}

data class PatternElementWithSepExp(val elem: PatternElement, val sep: PatternElement) : PatternExp() {
	constructor(elem: PatternElement, sep: PatternElement, isIgnore: Boolean): this(elem, sep) {
		this.isIgnore = isIgnore
	}
	override var isIgnore: Boolean = false
		private set

	override fun toToken() = WithSepToken(elem, sep).also { it.isIgnore = isIgnore }
}

sealed class PatternElement(val name: String) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PatternElement

		if (name != other.name) return false

		return true
	}

	override fun hashCode(): Int {
		return name.hashCode()
	}

	abstract fun toToken(): Token
}

class TokenElement(name: String) : PatternElement(name) {
	override fun toToken(): Token = NamedToken(this, if (Token.isTerminalName(name)) TokenType.Terminal else TokenType.NonTerminal)
}

data class StringElement(val value: String) : PatternElement("\"$value\"") {
	override fun toToken(): Token = StringToken(value)
}

sealed class TokenName {
	abstract val print: String
}

data class OfTokenName(val of: OfTokenNameOf, val name: PatternElement) : TokenName() {
	override val print: String by lazy { "$of<${name.name}>" }
}

data class SimpleTokenName(val name: PatternElement) : TokenName() {
	override val print: String by lazy { name.name }
}

enum class OfTokenNameOf {
	List, Optional
}
