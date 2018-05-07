package com.anatawa12.parser.frontend

import com.anatawa12.libs.collections.InitMap
import com.anatawa12.libs.collections.InitMutableBiMap
import com.anatawa12.libs.collections.InitMutableMap
import com.anatawa12.libs.collections.WarpGetThrowMap
import com.anatawa12.libs.util.plusAssign
import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.frontend.generated.SyntaxRunner
import com.anatawa12.parser.generator.ParserGenerator
import com.anatawa12.parser.parser.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Created by anatawa12 on 2018/04/19.
 */

object SyntaxDefectionGenerator {
	fun toString(x: PatternElement): String = when (x) {
		is TokenElement -> x.name
		is StringElement -> toTokenName(x.name)
	}

	fun toTokenName(string: String) = "\"$string\""

	@Throws(ReduceReduceConflictException::class)
	suspend fun generate(file: Kpt, args: KotlinPatternArguments): Pair<String, String> {
		val tops = file.tops
				.also { it.forEach { it.fromFile = args.input.name } }
				.toMutableList()

		var isError = false

		// "read Imports"
		var i = 0
		while (i < tops.size) {
			kotlin.run<Unit> w@{
				val import= (tops[i] as? ImportFile ?: return@w)
				val importFile = if (importFile.firstOrNull() != File.separatorChar){
					File(import.fromFile).parent + '/' + import.from
				} else import.from

				val file = File(importFile)
				if (file.exists()) {
					isError = true
					System.err.println("""not found "$importFile"""")
					return@w
				}

				val lexer = Lexer(file.inputStream().bufferedReader().readText())

				val runner = SyntaxRunner(lexer::lex, SyntaxAstGeneratorImpl)
				runner.run()
				tops += runner.result.tops.also { it.forEach { it.fromFile = file.name } }
			};i++
		}

		if (isError) System.exit(1)

		val patterns: MutableList<Pattern> = tops.filterIsInstanceTo(mutableListOf())

		val inputSyntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()
		val syntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()

		val zeroMores = mutableSetOf<String>()
		val oneMores = mutableSetOf<String>()
		val optionals = mutableSetOf<String>()
		val withSeps = mutableSetOf<Pair<String, String>>()

		// "generate syntaxDefinitions"

		patterns.flatMapTo(inputSyntaxDefinitions) { pattern ->
			pattern.pattern.map { elements ->
				SyntaxDefinitionSection(
						Token(pattern.lToken),
						elements.map { element ->
							Token(when (element) {
								is PatternElementExp -> toString(element.elem)
								is PatternElementZeroMoreExp -> toString(element.elem).also { zeroMores.add(it) } + "*"
								is PatternElementOneMoreExp -> toString(element.elem).also { oneMores.add(it) } + "+"
								is PatternElementOptionalExp -> toString(element.elem).also { optionals.add(it) } + "?"
								is PatternElementWithSepExp -> {
									val elem = toString(element.elem)
									val sep = toString(element.sep)
									withSeps.add(elem to sep)
									"$elem&$sep"
								}
							})
						},
						pattern.fromFile
				)
			}
		}

		syntaxDefinitions.addAll(inputSyntaxDefinitions)

		zeroMores.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(Token("$elem*"), listOf(), "<Generated>"),
					SyntaxDefinitionSection(Token("$elem*"), listOf(Token("$elem*"), Token(elem)), "<Generated>")
			)
		}

		oneMores.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(Token("$elem+"), listOf(Token(elem)), "<Generated>"),
					SyntaxDefinitionSection(Token("$elem+"), listOf(Token("$elem+"), Token(elem)), "<Generated>")
			)
		}

		optionals.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(Token("$elem?"), listOf(), "<Generated>"),
					SyntaxDefinitionSection(Token("$elem?"), listOf(Token(elem)), "<Generated>")
			)
		}

		withSeps.flatMapTo(syntaxDefinitions) { (elem, sep) ->
			listOf(
					SyntaxDefinitionSection(Token("$elem&$sep"), listOf(Token(elem)), "<Generated>"),
					SyntaxDefinitionSection(Token("$elem&$sep"), listOf(Token("$elem&$sep"), Token(sep), Token(elem)), "<Generated>")
			)
		}


		val skips = mutableSetOf<String>()

		// "read Skips"
		for (skip in tops.filterIsInstance<Skip>()) {
			skip.tokens.forEach { token ->
				skips.add(token)
			}
		}

		if (isError) System.exit(1)

		var varId = 0

		val unionMapType2Var = InitMutableBiMap<String, String> { "$${varId++}$" }
		val tokenNamesByVar = InitMap<String, MutableSet<TokenName>>({ mutableSetOf() })
		unionMapType2Var["Token"] = "token"

		fun getTokensByType(type: String) = tokenNamesByVar[unionMapType2Var[type]]
		fun getTypeByToken(token: TokenName): String {
			var result = unionMapType2Var.inverse()[tokenNamesByVar.entries.find { token in it.value }?.key]
			if (result == null) {
				result = "Token"
				getTokensByType("Token").add(token)
			}
			return result
		}

		fun getFieldByToken(token: TokenName) = tokenNamesByVar.entries.find { token in it.value }?.key!!

		// "make Unions and tokenNamesByVar"
		for (pattern in tops.filterIsInstance<Pattern>()) {
			getTokensByType(pattern.type).add(SimpleTokenName(pattern.lToken))
		}

		if (isError) System.exit(1)

		// "make types for *, +, ? and [] operators."
		for (token in zeroMores) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token))}>").add(OfTokenName("ListOf", token))
		}
		for (token in oneMores) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token))}>").add(OfTokenName("ListOf", token))
		}
		for ((token, _) in withSeps) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token))}>").add(OfTokenName("ListOf", token))
		}
		for (optional in optionals) {
			getTokensByType("${getTypeByToken(SimpleTokenName(optional))}?").add(OfTokenName("OptionalOf", optional))
		}

		if (isError) System.exit(1)

		val tokenTypeMap = mutableMapOf<String, String>()

		// "create tokenTypeMap"
		for (syntaxDefinition in inputSyntaxDefinitions) {
			tokenTypeMap[syntaxDefinition.ltoken.name] = getTypeByToken(SimpleTokenName(syntaxDefinition.ltoken.name))
		}
		for (token in zeroMores) {
			tokenTypeMap["$token*"] = getTypeByToken(OfTokenName("ListOf", token))
		}
		for (token in oneMores) {
			tokenTypeMap["$token+"] = getTypeByToken(OfTokenName("ListOf", token))
		}
		for ((token, sep) in withSeps) {
			tokenTypeMap["$token&$sep"] = getTypeByToken(OfTokenName("ListOf", token))
		}
		for (token in optionals) {
			tokenTypeMap["$token?"] = getTypeByToken(OfTokenName("OptionalOf", token))
		}

		if (isError) System.exit(1)

		val tokenFieldMap = InitMutableMap({ "token" }, mutableMapOf<String, String>())

		// "create tokenFieldMap"
		for (syntaxDefinition in inputSyntaxDefinitions) {
			val field = getFieldByToken(SimpleTokenName(syntaxDefinition.ltoken.name))
			tokenFieldMap[syntaxDefinition.ltoken.name] = field
		}
		for (token in zeroMores) {
			tokenFieldMap["$token*"] = getFieldByToken(OfTokenName("ListOf", token))
		}
		for (token in oneMores) {
			tokenFieldMap["$token+"] = getFieldByToken(OfTokenName("ListOf", token))
		}
		for ((token, sep) in withSeps) {
			tokenFieldMap["$token&$sep"] = getFieldByToken(OfTokenName("ListOf", token))
		}
		for (token in optionals) {
			tokenFieldMap["$token?"] = getFieldByToken(OfTokenName("OptionalOf", token))
		}

		if (isError) System.exit(1)

		val definitionSection = GrammarDefinition(syntaxDefinitions, Token(patterns.first().lToken))
		val generator = GenerateParsingTable(definitionSection, args)

		val imports = mutableSetOf<String>()
		// "make unionMapType2Var"
		for (import in tops.filterIsInstance<ImportPackeage>()) {
			imports.add(import.packageName)
		}


		var packageName: String? = null
		// "make unionMapType2Var"
		val packages = tops.filterIsInstance<Package>()
		packageName = if (packages.isEmpty()) "" else packages.singleOrNull()?.packageName

		if (packageName == null) {
			System.err.println("""found multiple @package""")
			exitProcess(1)
		}


		//generator.removeConflict()
		ParserGenerator.run {
			val builder = StringBuilder()
			builder += head(packageName, imports)
			builder += '\n'
			builder += token(generator.tokenSet)
			builder += '\n'
			builder += skips(skips)
			builder += '\n'
			builder += syntax(generator.syntaxes)
			builder += '\n'
			builder += syntaxAstGenerator(inputSyntaxDefinitions, tokenTypeMap)
			builder += '\n'
			builder += syntaxRunnner(inputSyntaxDefinitions, tokenFieldMap, WarpGetThrowMap(unionMapType2Var.inverse()), zeroMores, oneMores, optionals, withSeps)
			builder += '\n'
			builder += parsingTable(generator.parsingTable)
			builder += '\n'
			return builder.toString() to packageName
		}
	}
}
