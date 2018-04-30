package com.anatawa12.parser.frontend.ast

/**
 * Created by anatawa12 on 2018/04/17.
 */

data class Kpt(val tops: List<TopLevelObject>)

sealed class TopLevelObject()

data class Package(val packageName: String) : TopLevelObject()

data class ImportFile(val from: String) : TopLevelObject()

data class ImportPackeage(val packageName: String) : TopLevelObject()

data class Skip(val tokens: List<String>) : TopLevelObject()

data class Pattern(val type: String, val lToken: String, val pattern: List<PatternElements>) : TopLevelObject()

typealias PatternElements = List<PatternExp>

sealed class PatternExp()

data class PatternElementExp(val elem: PatternElement) : PatternExp()

data class PatternElementZeroMoreExp(val elem: PatternElement) : PatternExp()

data class PatternElementOneMoreExp(val elem: PatternElement) : PatternExp()

data class PatternElementOptionalExp(val elem: PatternElement) : PatternExp()

data class PatternElementWithSepExp(val elem: PatternElement, val sep: PatternElement) : PatternExp()

sealed class PatternElement()

data class TokenElement(val name: String) : PatternElement()

data class StringElement(val name: String) : PatternElement()

sealed class TokenName {
	abstract val print: String
}

data class OfTokenName(val of: String, val name: String) : TokenName() {
	override val print: String by lazy { "$of<$name>" }
}

data class SimpleTokenName(val name: String) : TokenName() {
	override val print: String by lazy { name }
}
