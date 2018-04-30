package com.anatawa12.parser.generator

import com.anatawa12.libs.collections.NotNullMap
import com.anatawa12.libs.collections.toMapList
import com.anatawa12.libs.util.escape
import com.anatawa12.parser.parser.Operation
import com.anatawa12.parser.parser.ParsingTable
import com.anatawa12.parser.parser.SyntaxDefinitions
import com.anatawa12.parser.parser.Token
import org.intellij.lang.annotations.Language

/**
 * Created by anatawa12 on 2018/04/15.
 */
object ParserGenerator {
	private val keywords = listOf(
			"as",
			"as?",
			"break",
			"class",
			"continue",
			"do",
			"else",
			"false",
			"for",
			"fun",
			"if",
			"in",
			"!in",
			"interface",
			"is",
			"!is",
			"null",
			"object",
			"package",
			"return",
			"super",
			"this",
			"throw",
			"true",
			"try",
			"typealias",
			"val",
			"var",
			"when",
			"while",
			"by",
			"catch",
			"constructor",
			"delegate",
			"dynamic",
			"field",
			"file",
			"finally",
			"get",
			"import",
			"init",
			"param",
			"property",
			"receiver",
			"set",
			"setparam",
			"where",
			"actual",
			"abstract",
			"annotation",
			"companion",
			"const",
			"crossinline",
			"data",
			"enum",
			"expect",
			"external",
			"final",
			"infix",
			"inline",
			"inner",
			"internal",
			"lateinit",
			"noinline",
			"open",
			"operator",
			"out",
			"override",
			"private",
			"protected",
			"public",
			"reified",
			"sealed",
			"suspend",
			"tailrec",
			"vararg"
	)
	fun String.varName() = if(this !in keywords && "^[\\p{javaLetter}_][\\p{javaLetterOrDigit}_]*$".toRegex().matches(this)) this else "`$this`"

	fun syntaxRunnner(syntaxDefections: SyntaxDefinitions, tokenFieldMap: NotNullMap<String, String>, unionMap: NotNullMap<String, String>, tokenIdMap: MutableMap<Token, Int>, zeroMores: MutableSet<String>, oneMores: MutableSet<String>, optionals: MutableSet<String>, withSeps: MutableSet<Pair<String, String>>): String {
		@Language("kotlin")
		val result = """
			class SyntaxRunner(private val _lexer: ()-> Token, val generator: SyntaxAstGenerator) {
				private val stack: Deque<StateWithClosureAndEntry> = LinkedList()
				private val _result = mutableListOf<Int>()
				private var isRan = false
				private var next: Token? = null
				private fun lexer(): TokenType {
					if (next == null) next = _lexer()
					return next!!.type
				}
				private fun read() {next = null}

				init {
					stack.push(StateWithClosureAndEntry(0, arrayOf(ClosureItem(-1, 0)), `${'$'}Union`(Token(TokenType.EOF, "", -1, -1))))
				}

				private fun runAInsn (): Boolean{
					val map = parsingTable[stack.peek().state]
					val insn = map[lexer()]
					when (insn) {
						is Operation.Shift -> {
							stack.push(StateWithClosureAndEntry(insn.to, insn.syntaxes, `${'$'}Union`(next!!)))
							read()
						}
						is Operation.Reduce -> {
							val syntaxId = insn.syntax
							val syntax = syntaxes[syntaxId]
							_result.add(syntaxId)
							val astList = mutableListOf<`${'$'}Union`>()
							repeat(syntax.pattern.size) { astList += stack.pop().entry }
							astList.reverse()
							val ast = when(syntax.hash) {${syntaxDefections.asSequence().mapIndexed { i, it ->  it.ltoken to it }.toMapList().asSequence().map { it.value.withIndex() }.flatMap { it.asSequence() }.joinToString (separator = ""){ (i, it) -> """
								"${tokenIdMap[it.ltoken]}:${it.pattern.joinToString(separator = ",") { tokenIdMap[it].toString() }}" -> `${'$'}Union`(generator.${it.ltoken.copy(name = "${it.ltoken.name}_$i").varName}(${it.pattern.withIndex().joinToString { (i, value) -> "astList[$i].${tokenFieldMap[value.name].varName()}" }}))"""}}
								// zeroMores${zeroMores.joinToString (separator = ""){ """
								"${tokenIdMap[Token("$it*")]}:" -> `${'$'}Union`(mutableListOf<${unionMap[tokenFieldMap[it]]}>())
								"${tokenIdMap[Token("$it*")]}:${tokenIdMap[Token("$it*")]},${tokenIdMap[Token(it)]}" -> `${'$'}Union`(astList[0].${tokenFieldMap["$it*"].varName()}.also { it.add(astList[1].${tokenFieldMap[it].varName()}) })"""}}
								// oneMores${oneMores.joinToString (separator = ""){ """
								"${tokenIdMap[Token("$it+")]}:${tokenIdMap[Token(it)]}" -> `${'$'}Union`(mutableListOf(astList[0].${tokenFieldMap[it]}))
								"${tokenIdMap[Token("$it+")]}:${tokenIdMap[Token("$it+")]},${tokenIdMap[Token(it)]}" -> `${'$'}Union`(astList[0].${tokenFieldMap["$it+"].varName()}.also { it.add(astList[1].${tokenFieldMap[it].varName()}) })"""}}
								// optionals${optionals.joinToString (separator = ""){ """
								"${tokenIdMap[Token("$it?")]}:" -> `${'$'}Union`(null as ${unionMap[tokenFieldMap["$it?"]]})
								"${tokenIdMap[Token("$it?")]}:${tokenIdMap[Token(it)]}" -> `${'$'}Union`(astList[0].${tokenFieldMap[it].varName()} as ${unionMap[tokenFieldMap["$it?"]]})"""}}
								// withSeps${withSeps.joinToString (separator = ""){ (it, sep) -> """
								"${tokenIdMap[Token("$it&$sep")]}:${tokenIdMap[Token(it)]}" -> `${'$'}Union`(mutableListOf(astList[0].${tokenFieldMap[it].varName()}))
								"${tokenIdMap[Token("$it&$sep")]}:${tokenIdMap[Token("$it&$sep")]},${tokenIdMap[Token(sep)]},${tokenIdMap[Token(it)]}" -> `${'$'}Union`(astList[0].${tokenFieldMap["$it&$sep"].varName()}.also { it.add(astList[2].${tokenFieldMap[it].varName()}) })"""}}
								else -> error("invalid Parser. Please create issue for Anatawa12Parser: ${'$'}syntax")
							}
							stack.push(StateWithClosureAndEntry((parsingTable[stack.peek().state][syntax.ltoken] as? Operation.Goto)?.to ?: error("invalid Parser. Please create issue for Anatawa12Parser"), insn.syntaxes, ast))
						}
						Operation.Accept -> {
							__result = stack.pop().entry.${tokenFieldMap[syntaxDefections.first().ltoken.name].varName()}
							return true
						}
						is Operation.Goto -> error("invalid Parser. Please create issue for Anatawa12Parser")
						null -> {
							if (lexer() !in skips) {
								error("invalid token: ${'$'}{lexer()} \n" +
										"\tat ${'$'}{next!!}\n" +
										"${'$'}{stack.peek().syntaxes.joinToString(separator = "\n") { "\tin ${'$'}{it.copy(dotIndex = it.dotIndex)}" }}\n" +
										"\tafter ${'$'}{_result.map { syntaxes[it] }}")
							}
							read()
						}
					}
					return false
				}

				fun run() {
					if (isRan) error("this ran")
					while (!runAInsn());
					isRan = true
				}

				private lateinit var __result: ${unionMap[tokenFieldMap[syntaxDefections.first().ltoken.name]]}
				val result: ${unionMap[tokenFieldMap[syntaxDefections.first().ltoken.name]]}
					get() {
						if (!isRan) error("this did't run.")
						return __result
					}

				private data class StateWithClosureAndEntry(val state: Int, val syntaxes: Array<ClosureItem>, val entry: `${'$'}Union`)

				private class `${'$'}Union` private constructor(val value: Any?, val type: UnionType) {${unionMap.entries.joinToString(separator = "") {(name, type) -> """
					val ${name.varName()}: $type get() = if(type == UnionType.${"_$name".varName()}) value as $type else error("my type is not $type")""" }}
					enum class UnionType{${unionMap.keys.joinToString(separator = ""){"""
						${"_$it".varName()},"""}}
					}

					companion object {${
			unionMap.entries.joinToString(separator = "") {(name, type) -> """
						@JvmName("invoke_$name")
						operator fun invoke(${"_$name".varName()}: $type) = `${'$'}Union`(${"_$name".varName()}, UnionType.${"_$name".varName()})""" }}
					}
				}
			}
		""".trimIndent()
		return result
	}

	fun skips(skips: Set<String>) : String {
		@Language("kotlin")
		val result = """
			private val skips: Set<TokenType> = setOf(${skips.joinToString(separator = ", "){ """
				TokenType.$it""" }}
			)
		""".trimIndent()
		return result
	}

	fun parsingTable(parsingTable: ParsingTable) : String {
		@Language("kotlin")
		val result = """
			private val parsingTable: ParsingTable = arrayOf(${parsingTable.withIndex().joinToString { (i, map) -> """
					makeMap$i()"""}}
			)${parsingTable.withIndex().joinToString(separator = "") { (i, map) -> """
			private fun makeMap$i() = mapOf(${map.entries.joinToString { (token, op) -> """
							${token.literal} to ${when (op) {
			is Operation.Shift -> "Operation.Shift(${op.to}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntaxId}, ${it.dotIndex})" }}))"
			is Operation.Reduce -> "Operation.Reduce(${op.syntax}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntaxId}, ${it.dotIndex})" }}))"
			is Operation.Conflicted -> TODO()
			Operation.Accept -> "Operation.Accept"
			is Operation.Goto -> "Operation.Goto(${op.to}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntaxId}, ${it.dotIndex})" }}))"
		}}""" }}
					)"""}}
		""".trimIndent()
		return result
	}

	fun syntax(syntaxDefections: SyntaxDefinitions) : String {
		@Language("kotlin")
		val result = """
			val syntaxes = createSyntax()
			fun createSyntax() = arrayOf<SyntaxDefinitionSection>(
					${syntaxDefections.joinToString(separator = ", \n\t\t\t\t\t"){ "SyntaxDefinitionSection(${it.ltoken.literal}, listOf(${it.pattern.joinToString { it.literal }}))" }}
			)
		""".trimIndent()
		return result
	}

	fun syntaxAstGenerator(syntaxDefections: SyntaxDefinitions, typeMap: Map<String, String>) : String {
		@Language("kotlin")
		val result = """
			interface SyntaxAstGenerator {${syntaxDefections.mapIndexed { i, it ->  it.ltoken to it }.toMapList().mapValues { it.value.withIndex() }.flatMap { it.value }.joinToString(separator = "") { (i, it) ->"""
				fun ${it.ltoken.copy(name = "${it.ltoken.name}_$i").varName}(${it.pattern.withIndex().joinToString { (i, it) -> "${it.copy(name = "${it.name}_$i").varName}: ${typeMap[it.name] ?: "Token"}" }}): ${typeMap[it.ltoken.name]}"""}}
			}
		""".trimIndent()
		return result
	}

	fun token(symbols: MutableMap<Token, Int>) : String {
		@Language("kotlin")
		val result = """
			enum class TokenType(val data: String){
				EOF("$"),
				${symbols.toList().sortedBy { it.second }.joinToString(separator = ", \n\t\t\t\t"){ (it) -> "${it.varName}(\"${it.name.escape()}\")" }}
			}
		""".trimIndent()
		return result
	}

	fun head(packageName: String, imports: Iterable<String>): String {
		@Language("kotlin")
		val result = """
			@file:Suppress("UNCHECKED_CAST", "EnumEntryName", "FunctionName", "ArrayInDataClass", "unused", "RedundantUnitReturnType", "MemberVisibilityCanBePrivate", "RemoveExplicitTypeArguments", "PrivatePropertyName")
${if (packageName != ""){"""
			package $packageName"""}else{""}}
${imports.joinToString(separator = ""){"""
			import $it"""}}
			import java.util.*

			typealias ParsingTable = Array<Map<TokenType, Operation>>

			sealed class Operation{
				class Shift(val to: Int, val syntaxes: Array<ClosureItem>) : Operation() {
					override fun toString(): String = "Shift(${'$'}to)"
				}
				class Goto(val to: Int, val syntaxes: Array<ClosureItem>) : Operation() {
					override fun toString(): String = "Goto(${'$'}to)"
				}
				class Reduce(val syntax: Int, val syntaxes: Array<ClosureItem>) : Operation() {
					override fun toString(): String = "Reduce(${'$'}syntax)"
				}
				object Accept : Operation() {
					override fun toString(): String = "Accept"
				}
			}

			data class ClosureItem constructor(
					val syntaxId: Int,
					val dotIndex: Int){
				private val toString: String by lazy {
					val syntax = syntaxes[syntaxId]
					val ltoken = syntax.ltoken
					val pattern = syntax.pattern.mapTo(mutableListOf()) { "\"${'$'}{it.data}\"" }
					pattern.add(dotIndex, ".")
					"ClosureItem(${'$'}{ltoken.data} -> ${'$'}{pattern.joinToString(separator = " ")})"
				}
				override fun toString(): String = toString
			}

			data class SyntaxDefinitionSection constructor(val ltoken: TokenType, val pattern: List<TokenType>) {
				override fun toString(): String {
					return "${'$'}{ltoken.data} -> ${'$'}{pattern.joinToString(separator = " "){"\"${'$'}{it.data}\""}}"
				}

				val hash = "${'$'}{ltoken.ordinal}:${'$'}{pattern.joinToString(separator = ",") { "${'$'}{it.ordinal}" }}"
			}

			data class Token(val type: TokenType, val data: String, val line: Int, val column: Int)
""".trimIndent()
		return result
	}

	private val Token.literal
		get() = "TokenType.$varName"
}
