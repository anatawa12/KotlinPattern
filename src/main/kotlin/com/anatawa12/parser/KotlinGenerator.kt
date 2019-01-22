package com.anatawa12.parser.generator

import com.anatawa12.libs.collections.NotNullMap
import com.anatawa12.libs.collections.toMapList
import com.anatawa12.libs.util.escape
import com.anatawa12.libs.util.plusAssign
import com.anatawa12.parser.frontend.KotlinPatternArguments
import com.anatawa12.parser.frontend.OutputType
import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.parser.*
import org.intellij.lang.annotations.Language
import java.io.File

/**
 * Created by anatawa12 on 2018/04/15.
 */
object KotlinGenerator {
	fun generates(
			packageName: String,
			imports: MutableSet<String>,
			symbols: Set<Token>,
			skips: MutableSet<Token>,
			syntaxDefections: SyntaxDefinitions,
			inputSyntaxDefinitions: MutableList<SyntaxDefinitionSection>,
			typeMap: Map<Token, String>,
			tokenFieldMap: NotNullMap<Token, String>,
			unionMap: NotNullMap<String, String>,
			zeroMores: MutableSet<PatternElementZeroMoreExp>,
			oneMores: MutableSet<PatternElementOneMoreExp>,
			optionals: MutableSet<PatternElementOptionalExp>,
			withSeps: MutableSet<PatternElementWithSepExp>,
			parsingTable: ParsingTable,
			args: KotlinPatternArguments) {

		val builder = StringBuilder()
		builder += head(packageName, imports)
		builder += '\n'
		builder += token(symbols)
		builder += '\n'
		builder += skips(skips)
		builder += '\n'
		builder += syntax(syntaxDefections)
		builder += '\n'
		builder += syntaxAstGenerator(inputSyntaxDefinitions, typeMap)
		builder += '\n'
		builder += syntaxRunnner(inputSyntaxDefinitions, tokenFieldMap, unionMap, zeroMores, oneMores, optionals, withSeps)
		builder += '\n'
		builder += parsingTable(parsingTable)
		builder += '\n'

		val src = builder.toString()

		when (args.outPutType) {
			OutputType.PackageRootDir -> {
				val dir = File(args.output, packageName.replace('.', File.separatorChar)).also { it.mkdirs() }
				File(dir, "Parser.kt").outputStream().write(src.toByteArray())
			}
			OutputType.FilePath -> {
				args.output.mkdirs()
				val parsingFile = File(args.output, "Parser.kt")
				if (!parsingFile.exists()) {
					parsingFile.createNewFile()
				}
				parsingFile.outputStream().write(src.toByteArray())
			}
		}
	}

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
	fun String.varName() = if(this !in keywords && "^[\\p{javaLetter}_][\\p{javaLetterOrDigit}_]*$".toRegex().matches(this)) this else {

		val varName = replace("!", "!!")
				.replace(".", "!d")//Dot
				.replace(";", "!s")//Semi
				.replace(":", "!c")//Colon
				.replace("[", "!ls")//Left Square bracket
				.replace("]", "!rs")//Right Square bracket
				.replace("<", "!la")//Left Angle bracket
				.replace(">", "!ra")//Right Angle bracket
				.replace("\\", "!h")//backslasH
				.replace("/", "!f")//Forward slash
				.replace("\b", "!b")
				.replace("\n", "!n")
				.replace("\r", "!r")
				.replace("\t", "!t")
		"`$varName`"
	}

	fun syntaxRunnner(syntaxDefections: SyntaxDefinitions, tokenFieldMap: NotNullMap<Token, String>, unionMap: NotNullMap<String, String>, zeroMores: MutableSet<PatternElementZeroMoreExp>, oneMores: MutableSet<PatternElementOneMoreExp>, optionals: MutableSet<PatternElementOptionalExp>, withSeps: MutableSet<PatternElementWithSepExp>): String {
		@Language("kotlin")
		val result = """
			class SyntaxRunner(private val _lexer: ()-> Token, val generator: ISyntaxAstGenerator) {
				private val stack: MutableList<StateWithClosureAndEntry> = mutableListOf()
				private val _result = mutableListOf<Int>()
				private var isRan = false
				private var next: Token? = null
				private fun lexer(): TokenType {
					if (next == null) next = _lexer()
					return next!!.type
				}
				private fun read() {next = null}

				private fun <T>MutableList<T>.push(e: T) { add(e) }
				private fun <T>MutableList<T>.pop() = removeAt(lastIndex)
				private fun <T>MutableList<T>.peek() = last()

				init {
					stack.push(StateWithClosureAndEntry(0, arrayOf(ClosureItem(0, 0)), `${'$'}Union`(Token(${EofToken.enumLiteral}, "", -1, -1))))
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
							val ast = when(syntax.hash) {${syntaxDefections.asSequence().map { it.ltoken to it }.toMapList().asSequence().map { it.value.withIndex() }.flatMap { it.asSequence() }.joinToString (separator = ""){ (i, it) -> """
								"${it.ltoken.resultId}:${it.pattern.joinToString(separator = ",") { it.resultId.toString() }}" -> `${'$'}Union`(generator.${"${it.ltoken.viewName}_$i".varName()}(${it.pattern.withIndex().filterNot { (_, v) -> v.isIgnore }.joinToString { (i, value) -> "astList[$i].${tokenFieldMap[value].varName()}" }}))"""}}
								// zeroMores${zeroMores.joinToString (separator = ""){ """
								"${it.toToken().resultId}:" -> `${'$'}Union`(mutableListOf<${unionMap[tokenFieldMap[it.elem.toToken()]]}>())
								"${it.toToken().resultId}:${it.toToken().resultId},${it.elem.toToken().resultId}" -> `${'$'}Union`(astList[0].${tokenFieldMap[it.toToken()].varName()}.also { it.add(astList[1].${tokenFieldMap[it.elem.toToken()].varName()}) })"""}}
								// oneMores${oneMores.joinToString (separator = ""){ """
								"${it.toToken().resultId}:${it.elem.toToken().resultId}" -> `${'$'}Union`(mutableListOf(astList[0].${tokenFieldMap[it.elem.toToken()].varName()}))
								"${it.toToken().resultId}:${it.toToken().resultId},${it.elem.toToken().resultId}" -> `${'$'}Union`(astList[0].${tokenFieldMap[it.toToken()].varName()}.also { it.add(astList[1].${tokenFieldMap[it.elem.toToken()].varName()}) })"""}}
								// optionals${optionals.joinToString (separator = ""){ """
								"${it.toToken().resultId}:" -> `${'$'}Union`(null as ${unionMap[tokenFieldMap[it.toToken()]]})
								"${it.toToken().resultId}:${it.elem.toToken().resultId}" -> `${'$'}Union`(astList[0].${tokenFieldMap[it.elem.toToken()].varName()} as ${unionMap[tokenFieldMap[it.toToken()]]})"""}}
								// withSeps${withSeps.joinToString (separator = ""){ it -> """
								"${it.toToken().resultId}:${it.elem.toToken().resultId}" -> `${'$'}Union`(mutableListOf(astList[0].${tokenFieldMap[it.elem.toToken()].varName()}))
								"${it.toToken().resultId}:${it.toToken().resultId},${it.sep.toToken().resultId},${it.elem.toToken().resultId}" -> `${'$'}Union`(astList[0].${tokenFieldMap[it.toToken()].varName()}.also { it.add(astList[2].${tokenFieldMap[it.elem.toToken()].varName()}) })"""}}
								else -> error("invalid Parser. Please create issue for Anatawa12Parser: ${'$'}{syntax.debug()}")
							}
							stack.push(StateWithClosureAndEntry((parsingTable[stack.peek().state][syntax.ltoken] as? Operation.Goto)?.to ?: error("invalid Parser. Please create issue for Anatawa12Parser"), insn.syntaxes, ast))
						}
						Operation.Accept -> {
							__result = stack.pop().entry.${tokenFieldMap[syntaxDefections.first().ltoken].varName()}
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

				private lateinit var __result: ${unionMap[tokenFieldMap[syntaxDefections.first().ltoken]]}
				val result: ${unionMap[tokenFieldMap[syntaxDefections.first().ltoken]]}
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
						operator fun invoke(value: $type) = `${'$'}Union`(value, UnionType.${"_$name".varName()})""" }}
					}
				}
			}
		""".trimIndent()
		return result
	}

	fun skips(skips: MutableSet<Token>) : String {
		@Language("kotlin")
		val result = """
			private val skips: Set<TokenType> = setOf(${skips.joinToString(separator = ", "){ """
				${it.enumLiteral}""" }}
			)
		""".trimIndent()
		return result
	}

	fun parsingTable(parsingTable: ParsingTable) : String {
		@Language("kotlin")
		val result = """
			private val parsingTable: ParsingTable = arrayOf(${parsingTable.withIndex().joinToString { (i, _) -> """
					makeMap$i()"""}}
			)${parsingTable.withIndex().joinToString(separator = "") { (i, map) -> """
			private fun makeMap$i() = mapOf(${map.entries.joinToString { (token, op) -> """
							${token.enumLiteral} to ${when (op) {
			is Operation.Shift -> "Operation.Shift(${op.to}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntax.id}, ${it.dotIndex})" }}))"
			is Operation.Reduce -> "Operation.Reduce(${op.syntax}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntax.id}, ${it.dotIndex})" }}))"
			is Operation.Conflicted -> TODO()
			Operation.Accept -> "Operation.Accept"
			is Operation.Goto -> "Operation.Goto(${op.to}, arrayOf(${op.syntaxes.joinToString { "ClosureItem(${it.syntax.id}, ${it.dotIndex})" }}))"
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
					${syntaxDefections.joinToString(separator = ", \n\t\t\t\t\t"){ "SyntaxDefinitionSection(${it.ltoken.enumLiteral}, listOf(${it.pattern.joinToString { it.enumLiteral }}))" }}
			)
		""".trimIndent()
		return result
	}

	fun syntaxAstGenerator(syntaxDefections: SyntaxDefinitions, typeMap: Map<Token, String>) : String {
		@Language("kotlin")
		val result = """
			interface ISyntaxAstGenerator {${syntaxDefections.mapIndexed { i, it ->  it.ltoken to it }.toMapList().mapValues { it.value.withIndex() }.flatMap { it.value }.joinToString(separator = "") { (i, it) ->"""
				fun ${"${it.ltoken.name}_$i".varName()}(${it.pattern.withIndex().filterNot { (_, v) -> v.isIgnore }.joinToString { (i, it) -> "${"arg$i".varName()}: ${typeMap[it] ?: "Token"}" }}): ${typeMap[it.ltoken]}"""}}
			}
		""".trimIndent()
		return result
	}

	fun token(symbols: Set<Token>) : String {
		@Language("kotlin")
		val result = """
			enum class TokenType(val data: String){
				${symbols.sortedBy { it.resultId }.joinToString(separator = ", \n\t\t\t\t"){ "${it.viewName.varName()}(\"${it.name.escape()}\")" }}
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

				fun debug(): String {
					return "${'$'}hash is ${'$'}{toString()}"
				}

				val hash = "${'$'}{ltoken.ordinal}:${'$'}{pattern.joinToString(separator = ",") { "${'$'}{it.ordinal}" }}"
			}

			data class Token(val type: TokenType, val data: String, val line: Int, val column: Int)
""".trimIndent()
		return result
	}

	private val Token.enumLiteral
		get() = "TokenType.${viewName.varName()}"
}
