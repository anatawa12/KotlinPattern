package com.anatawa12.parser.frontend

import com.anatawa12.libs.collections.*
import com.anatawa12.parser.KotlinGenerator
import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.frontend.generated.SyntaxRunner
import com.anatawa12.parser.parser.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Created by anatawa12 on 2018/04/19.
 */

object SyntaxDefectionGenerator {
	@Throws(ReduceReduceConflictException::class)
	suspend fun generate(inFile: Kpt, args: KotlinPatternArguments): Unit {
		val tops = inFile.tops
				.also { it.forEach { it.fromFile = args.input.name } }
				.toMutableList()

		var isError = false

		//region read Imports
		for (topLevelObject in tops.changeableIterator()) {
			val import= topLevelObject as? ImportFile ?: continue
			val importFile = if (importFile.firstOrNull() != File.separatorChar){
				File(import.fromFile).parent + '/' + import.from
			} else import.from

			val file = File(importFile)
			if (!file.exists()) {
				isError = true
				System.err.println("""not found "$importFile"""")
				continue
			}

			val lexer = Lexer(file.inputStream().bufferedReader().readText())

			val runner = SyntaxRunner(lexer::lex, SyntaxAstGeneratorImpl)
			runner.run()
			tops += runner.result.tops.also { it.forEach { it.fromFile = file.name } }
		}
		if (isError) System.exit(1)

		//endregion

		val patterns: MutableList<Pattern> = tops.filterIsInstanceTo(mutableListOf())

		val syntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()

		val zeroMores = mutableSetOf<PatternElementZeroMoreExp>()
		val oneMores = mutableSetOf<PatternElementOneMoreExp>()
		val optionals = mutableSetOf<PatternElementOptionalExp>()
		val withSeps = mutableSetOf<PatternElementWithSepExp>()


		//region check terminals

		run<Unit> {
			val nonTerminals = mutableSetOf<String>()
			patterns.mapTo(nonTerminals) { it.lToken }
			val patternElements = mutableSetOf<PatternElement>()
			patterns.forEach { pattern ->
				pattern.pattern.forEach { elements ->
					elements.forEach { exp ->
						when (exp) {
							is PatternElementExp -> patternElements.add(exp.elem)
							is PatternElementZeroMoreExp -> patternElements.add(exp.elem)
							is PatternElementOneMoreExp -> patternElements.add(exp.elem)
							is PatternElementOptionalExp -> patternElements.add(exp.elem)
							is PatternElementWithSepExp -> {
								patternElements.add(exp.elem)
								patternElements.add(exp.sep)
							}
						}
					}
				}
			}
			patternElements.asSequence()
					.filterIsInstance<TokenElement>()
					.filterNot { it.name in nonTerminals }
					.forEach { Token.addTerminalName(it.name) }
			EofToken.viewName = "EOF"
		}

		//endregion

		//region string value

		val stringNameMap = mutableMapOf<String, String>()

		tops.filterIsInstance<StringName>().forEach { stringNameMap[it.string] = it.name }
		Token.viewNameMap.putAll(stringNameMap)

		//endregion

		//region generate syntaxDefinitions

		val inputSyntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()

		patterns.flatMapTo(inputSyntaxDefinitions) { pattern ->
			pattern.pattern.map { elements ->
				SyntaxDefinitionSection(
						pattern.lTokenToken,
						elements.map {
							when (it) {
								is PatternElementExp -> {  }
								is PatternElementZeroMoreExp -> { zeroMores.add(it) }
								is PatternElementOneMoreExp -> { oneMores.add(it) }
								is PatternElementOptionalExp -> { optionals.add(it) }
								is PatternElementWithSepExp -> { withSeps.add(it) }
							}
							it.toToken()
						},
						pattern.fromFile
				)
			}
		}

		syntaxDefinitions.addAll(inputSyntaxDefinitions)

		zeroMores.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(ZeroMoreToken(elem.elem), listOf(), "<Generated>"),
					SyntaxDefinitionSection(ZeroMoreToken(elem.elem), listOf(ZeroMoreToken(elem.elem), elem.elem.toToken()), "<Generated>")
			)
		}

		oneMores.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(OneMoreToken(elem.elem), listOf(elem.elem.toToken()), "<Generated>"),
					SyntaxDefinitionSection(OneMoreToken(elem.elem), listOf(OneMoreToken(elem.elem), elem.elem.toToken()), "<Generated>")
			)
		}

		optionals.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(OptionalToken(elem.elem), listOf(), "<Generated>"),
					SyntaxDefinitionSection(OptionalToken(elem.elem), listOf(elem.elem.toToken()), "<Generated>")
			)
		}

		withSeps.flatMapTo(syntaxDefinitions) { elem ->
			listOf(
					SyntaxDefinitionSection(WithSepToken(elem.elem, elem.sep), listOf(elem.elem.toToken()), "<Generated>"),
					SyntaxDefinitionSection(WithSepToken(elem.elem, elem.sep), listOf(WithSepToken(elem.elem, elem.sep), elem.sep.toToken(), elem.elem.toToken()), "<Generated>")
			)
		}

		//endregion


		//region read Skips
		val skips = mutableSetOf<Token>()

		for (skip in tops.filterIsInstance<Skip>()) {
			skip.tokens.forEach { token ->
				skips.add(NamedToken(TokenElement(token), TokenType.Terminal))
			}
		}

		if (isError) System.exit(1)

		//endregion

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

		//region make Unions and tokenNamesByVar
		for (pattern in tops.filterIsInstance<Pattern>()) {
			getTokensByType(pattern.type).add(SimpleTokenName(pattern.lTokenToken.baseElement))
		}

		//endregion

		//region make types for *, +, ? and [] operators.
		for (token in zeroMores) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token.elem))}>").add(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in oneMores) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token.elem))}>").add(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for ((token, _) in withSeps) {
			getTokensByType("MutableList<${getTypeByToken(SimpleTokenName(token))}>").add(OfTokenName(OfTokenNameOf.List, token))
		}
		for (optional in optionals) {
			getTokensByType("${getTypeByToken(SimpleTokenName(optional.elem))}?").add(OfTokenName(OfTokenNameOf.Optional, optional.elem))
		}

		if (isError) System.exit(1)

		//endregion

		//region create tokenTypeMap

		val tokenTypeMap = mutableMapOf<Token, String>()

		for (syntaxDefinition in inputSyntaxDefinitions) {
			tokenTypeMap[syntaxDefinition.ltoken] = getTypeByToken(SimpleTokenName((syntaxDefinition.ltoken as NamedToken).baseElement))
		}
		for (token in zeroMores) {
			tokenTypeMap[token.toToken()] = getTypeByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in oneMores) {
			tokenTypeMap[token.toToken()] = getTypeByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in withSeps) {
			tokenTypeMap[token.toToken()] = getTypeByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in optionals) {
			tokenTypeMap[token.toToken()] = getTypeByToken(OfTokenName(OfTokenNameOf.Optional, token.elem))
		}

		if (isError) System.exit(1)

		//endregion

		//region create tokenFieldMap

		val tokenFieldMap = InitMutableMap({ "token" }, mutableMapOf<Token, String>())

		for (syntaxDefinition in inputSyntaxDefinitions) {
			val field = getFieldByToken(SimpleTokenName((syntaxDefinition.ltoken as NamedToken).baseElement))
			tokenFieldMap[syntaxDefinition.ltoken] = field
		}
		for (token in zeroMores) {
			tokenFieldMap[token.toToken()] = getFieldByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in oneMores) {
			tokenFieldMap[token.toToken()] = getFieldByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in withSeps) {
			tokenFieldMap[token.toToken()] = getFieldByToken(OfTokenName(OfTokenNameOf.List, token.elem))
		}
		for (token in optionals) {
			tokenFieldMap[token.toToken()] = getFieldByToken(OfTokenName(OfTokenNameOf.Optional, token.elem))
		}

		if (isError) System.exit(1)

		//endregion

		val definitionSection = GrammarDefinition(syntaxDefinitions, patterns.first().lTokenToken)
		val generator = GenerateParsingTable(definitionSection, args)

		//region make header

		val imports = mutableSetOf<String>()

		for (import in tops.filterIsInstance<ImportPackeage>()) {
			imports.add(import.packageName)
		}

		//endregion

		//region make unionMapType2Var

		var packageName: String?

		val packages = tops.filterIsInstance<Package>()
		packageName = if (packages.isEmpty()) "" else packages.singleOrNull()?.packageName

		if (packageName == null) {
			System.err.println("""found multiple @package""")
			exitProcess(1)
		}

		//endregion

		KotlinGenerator.generates(packageName, imports, generator.tokenSet, skips, generator.syntaxes, inputSyntaxDefinitions, tokenTypeMap, tokenFieldMap, WarpGetThrowMap(unionMapType2Var.inverse()), zeroMores, oneMores, optionals, withSeps, generator.parsingTable, args)
	}
}
