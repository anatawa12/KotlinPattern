package com.anatawa12.parser.parser

import com.anatawa12.parser.frontend.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by anatawa12 on 2018/05/03.
 */
class ParsingTableGenerateKtTest {
	val expectedTokenSet = setOf(
			Token.Eof,
			Token.Syntax,
			Token(SimpleName),
			Token(string),
			Token(kptFile),
			Token(TopLebelObject),
			Token(patType),
			Token(importFile),
			Token(importPackage),
			Token(skip),
			Token(pattern),
			Token(SEMI),
			Token(typeKt),
			Token(orPatterns),
			Token(patterns),
			Token(PatternElements),
			Token(PatternExp),
			Token(PatternElement),
			Token(LF),
			Token(typeReference),
			Token(userType),
			Token(simpleUserType),
			Token(projection),
			Token(varianceAnnotation),
			Token(projectionType),
			Token(projectionTypeWithConnma),
			Token(nullableType),
			Token(`package`),
			Token(`TopLebelObject*`),
			Token(`PatternExp*`),
			Token(`SEMI?`),
			Token(`patType?`),
			Token(`SimpleName$with$stringLiteral$$,`),
			Token(`SimpleName$with$stringLiteral$$dot`),
			Token(`PatternElements$with$stringLiteral$$|`),
			Token(`PatternElements$with$stringLiteral$$colon`),
			Token(string("@package")),
			Token(string("@import")),
			Token(string("(")),
			Token(string(")")),
			Token(string(".")),
			Token(string("*")),
			Token(string("as")),
			Token(string("@skip")),
			Token(string(":")),
			Token(string("=")),
			Token(string(";")),
			Token(string("+")),
			Token(string("?")),
			Token(string("{")),
			Token(string("}")),
			Token(string("<")),
			Token(string(">")),
			Token(string(",")),
			Token(string("in")),
			Token(string("out")),
			Token(string("\r")),
			Token(string("\n")),
			Token(string("\r\n")),
			Token(string("|"))
	)

	@Test
	fun setEveryTokenAndDefinitionIdResult() {
		val tokenSet = setEveryTokenAndDefinitionId(syntaxDefinitions)
		assertThat(tokenSet, `is`(expectedTokenSet))
	}

	@Test
	fun setEveryTokenAndDefinitionIdMap() {
		val tokenSet = setEveryTokenAndDefinitionId(syntaxDefinitions)
		for (token in tokenSet) {
			assertThat(token.resultId, `is`(token.runTimeId - 1))
			assertThat(token.runTimeId, `is`(not(0)))
			token.type
		}
	}

	val expectedNullSet = setOf(
			Token(`SEMI?`),
			Token(`patType?`),
			Token(`PatternExp*`),
			Token(`TopLebelObject*`),
			Token(kptFile),
			Token.Syntax,
			Token(PatternElements),
			Token(`PatternElements$with$stringLiteral$$colon`),
			Token(`PatternElements$with$stringLiteral$$|`),
			Token(orPatterns),
			Token(projection)
	)

	@Test
	fun generateNullSet() {
		assertThat(generateNullSet(syntaxDefinitions), `is`(expectedNullSet))
	}

	val expectedFirstMap = FirstSet(mapOf(
			Token(`SEMI?`) to setOf(Token(string("\r")), Token(string("\n")), Token(string("\r\n")), Token(string(";"))),
			Token(`patType?`) to setOf(Token(string(":"))),
			Token(`PatternExp*`) to setOf(Token(string), Token(SimpleName)),
			Token(`TopLebelObject*`) to setOf(Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package"))),
			Token(`PatternElements$with$stringLiteral$$colon`) to setOf(Token(string), Token(SimpleName), Token(string(":"))),
			Token(`PatternElements$with$stringLiteral$$|`) to setOf(Token(string), Token(SimpleName), Token(string("|"))),
			Token(`SimpleName$with$stringLiteral$$dot`) to setOf(Token(SimpleName)),
			Token(`SimpleName$with$stringLiteral$$,`) to setOf(Token(SimpleName)),
			Token(SEMI) to setOf(Token(string("\r")), Token(string("\n")), Token(string("\r\n")), Token(string(";"))),
			Token(LF) to setOf(Token(string("\r")), Token(string("\n")), Token(string("\r\n"))),
			Token(varianceAnnotation) to setOf(Token(string("in")), Token(string("out"))),
			Token(projection) to setOf(Token(string("in")), Token(string("out"))),
			Token(projectionTypeWithConnma) to setOf(Token(string("*")), Token(string("in")), Token(string("out")), Token(string("(")), Token(SimpleName)),
			Token(projectionType) to setOf(Token(string("*")), Token(string("in")), Token(string("out")), Token(string("(")), Token(SimpleName)),
			Token(simpleUserType) to setOf(Token(SimpleName)),
			Token(userType) to setOf(Token(SimpleName)),
			Token(nullableType) to setOf(Token(string("(")), Token(SimpleName)),
			Token(typeReference) to setOf(Token(string("(")), Token(SimpleName)),
			Token(typeKt) to setOf(Token(string("(")), Token(SimpleName)),
			Token(PatternElement) to setOf(Token(string), Token(SimpleName)),
			Token(PatternExp) to setOf(Token(string), Token(SimpleName)),
			Token(PatternElements) to setOf(Token(string), Token(SimpleName)),
			Token(patterns) to setOf(Token(string(":"))),
			Token(orPatterns) to setOf(Token(string), Token(SimpleName), Token(string("|"))),
			Token(pattern) to setOf(Token(SimpleName)),
			Token(patType) to setOf(Token(string(":"))),
			Token(skip) to setOf(Token(string("@skip"))),
			Token(importPackage) to setOf(Token(string("@import"))),
			Token(importFile) to setOf(Token(string("@import"))),
			Token(`package`) to setOf(Token(string("@package"))),
			Token(TopLebelObject) to setOf(Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package"))),
			Token(kptFile) to setOf(Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package"))),
			Token(string("@package")) to setOf(Token(string("@package"))),
			Token(string("@import")) to setOf(Token(string("@import"))),
			Token(string("(")) to setOf(Token(string("("))),
			Token(string(")")) to setOf(Token(string(")"))),
			Token(string(".")) to setOf(Token(string("."))),
			Token(string("*")) to setOf(Token(string("*"))),
			Token(string("as")) to setOf(Token(string("as"))),
			Token(string("@skip")) to setOf(Token(string("@skip"))),
			Token(string(":")) to setOf(Token(string(":"))),
			Token(string("=")) to setOf(Token(string("="))),
			Token(string(";")) to setOf(Token(string(";"))),
			Token(string("+")) to setOf(Token(string("+"))),
			Token(string("?")) to setOf(Token(string("?"))),
			Token(string("{")) to setOf(Token(string("{"))),
			Token(string("}")) to setOf(Token(string("}"))),
			Token(string("<")) to setOf(Token(string("<"))),
			Token(string(">")) to setOf(Token(string(">"))),
			Token(string(",")) to setOf(Token(string(","))),
			Token(string("in")) to setOf(Token(string("in"))),
			Token(string("out")) to setOf(Token(string("out"))),
			Token(string("\r")) to setOf(Token(string("\r"))),
			Token(string("\n")) to setOf(Token(string("\n"))),
			Token(string("\r\n")) to setOf(Token(string("\r\n"))),
			Token(string("|")) to setOf(Token(string("|"))),
			Token.Eof to setOf(Token.Eof),
			Token.Syntax to setOf(Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package"))),
			Token(SimpleName) to setOf(Token(SimpleName)),
			Token(string) to setOf(Token(string))
	), expectedNullSet)

	@Test
	fun generateFirstMap() {
		setEveryTokenAndDefinitionId(syntaxDefinitions)
		expectedTokenSet.forEach(Token::setIdAndTypeFromMap)
		assertThat(generateFirstMap(syntaxDefinitions, expectedNullSet, expectedTokenSet), `is`(expectedFirstMap))
	}

	val expectedSyntaxDB: SyntaxDB = mapOf(
			Token.Syntax to listOf(
					SyntaxDefinitionSection(Token.Syntax, listOf(Token(kptFile)))
			),
			Token(kptFile) to listOf(
					SyntaxDefinitionSection(Token(kptFile), listOf(Token(`TopLebelObject*`)))
			),
			Token(TopLebelObject) to listOf(
					SyntaxDefinitionSection(Token(TopLebelObject), listOf(Token(importFile))),
					SyntaxDefinitionSection(Token(TopLebelObject), listOf(Token(pattern))),
					SyntaxDefinitionSection(Token(TopLebelObject), listOf(Token(skip))),
					SyntaxDefinitionSection(Token(TopLebelObject), listOf(Token(importPackage))),
					SyntaxDefinitionSection(Token(TopLebelObject), listOf(Token(`package`)))
			),
			Token(`package`) to listOf(
					SyntaxDefinitionSection(Token(`package`), listOf(Token(string("@package")), Token(`SimpleName$with$stringLiteral$$dot`)))
					),
			Token(importFile) to listOf(
					SyntaxDefinitionSection(Token(importFile), listOf(Token(string("@import")), Token(string("(")), Token(string), Token(string(")"))))
			),
			Token(importPackage) to listOf(
					SyntaxDefinitionSection(Token(importPackage), listOf(Token(string("@import")), Token(`SimpleName$with$stringLiteral$$dot`))),
					SyntaxDefinitionSection(Token(importPackage), listOf(Token(string("@import")), Token(`SimpleName$with$stringLiteral$$dot`), Token(string(".")), Token(string("*")))),
					SyntaxDefinitionSection(Token(importPackage), listOf(Token(string("@import")), Token(`SimpleName$with$stringLiteral$$dot`), Token(string("as")), Token(SimpleName)))
			),
			Token(skip) to listOf(
					SyntaxDefinitionSection(Token(skip), listOf(Token(string("@skip")), Token(`SimpleName$with$stringLiteral$$,`)))
			),
			Token(patType) to listOf(
					SyntaxDefinitionSection(Token(patType), listOf(Token(string(":")), Token(typeKt)))
			),
			Token(pattern) to listOf(
					SyntaxDefinitionSection(Token(pattern), listOf(Token(SimpleName), Token(`patType?`), Token(SEMI), Token(string("=")), Token(orPatterns), Token(string(";")))),
					SyntaxDefinitionSection(Token(pattern), listOf(Token(SimpleName), Token(`patType?`), Token(SEMI), Token(patterns), Token(string(";"))))
			),
			Token(orPatterns) to listOf(
					SyntaxDefinitionSection(Token(orPatterns), listOf(Token(`PatternElements$with$stringLiteral$$|`)))
			),
			Token(patterns) to listOf(
					SyntaxDefinitionSection(Token(patterns), listOf(Token(string(":")), Token(`PatternElements$with$stringLiteral$$colon`)))
			),
			Token(PatternElements) to listOf(
					SyntaxDefinitionSection(Token(PatternElements), listOf(Token(`PatternExp*`)))
			),
			Token(PatternExp) to listOf(
					SyntaxDefinitionSection(Token(PatternExp), listOf(Token(PatternElement))),
					SyntaxDefinitionSection(Token(PatternExp), listOf(Token(PatternElement), Token(string("*")))),
					SyntaxDefinitionSection(Token(PatternExp), listOf(Token(PatternElement), Token(string("+")))),
					SyntaxDefinitionSection(Token(PatternExp), listOf(Token(PatternElement), Token(string("?")))),
					SyntaxDefinitionSection(Token(PatternExp), listOf(Token(PatternElement), Token(string("{")), Token(PatternElement), Token(string("}"))))
			),
			Token(PatternElement) to listOf(
					SyntaxDefinitionSection(Token(PatternElement), listOf(Token(string))),
					SyntaxDefinitionSection(Token(PatternElement), listOf(Token(SimpleName)))
			),
			Token(typeKt) to listOf(
					SyntaxDefinitionSection(Token(typeKt), listOf(Token(typeReference)))
			),
			Token(typeReference) to listOf(
					SyntaxDefinitionSection(Token(typeReference), listOf(Token(string("(")), Token(typeReference), Token(string(")")))),
					SyntaxDefinitionSection(Token(typeReference), listOf(Token(userType))),
					SyntaxDefinitionSection(Token(typeReference), listOf(Token(nullableType)))
			),
			Token(nullableType) to listOf(
					SyntaxDefinitionSection(Token(nullableType), listOf(Token(typeReference), Token(string("?"))))
			),
			Token(userType) to listOf(
					SyntaxDefinitionSection(Token(userType), listOf(Token(simpleUserType))),
					SyntaxDefinitionSection(Token(userType), listOf(Token(userType), Token(string(".")), Token(simpleUserType)))
			),
			Token(simpleUserType) to listOf(
					SyntaxDefinitionSection(Token(simpleUserType), listOf(Token(SimpleName))),
					SyntaxDefinitionSection(Token(simpleUserType), listOf(Token(SimpleName), Token(string("<")), Token(projectionTypeWithConnma), Token(string(">"))))
			),
			Token(projectionType) to listOf(
					SyntaxDefinitionSection(Token(projectionType), listOf(Token(projection), Token(typeKt))),
					SyntaxDefinitionSection(Token(projectionType), listOf(Token(string("*"))))
			),
			Token(projectionTypeWithConnma) to listOf(
					SyntaxDefinitionSection(Token(projectionTypeWithConnma), listOf(Token(projectionType))),
					SyntaxDefinitionSection(Token(projectionTypeWithConnma), listOf(Token(projectionTypeWithConnma), Token(string(",")), Token(projectionType)))
			),
			Token(projection) to listOf(
					SyntaxDefinitionSection(Token(projection), listOf()),
					SyntaxDefinitionSection(Token(projection), listOf(Token(varianceAnnotation)))
			),
			Token(varianceAnnotation) to listOf(
					SyntaxDefinitionSection(Token(varianceAnnotation), listOf(Token(string("in")))),
					SyntaxDefinitionSection(Token(varianceAnnotation), listOf(Token(string("out"))))
			),
			Token(LF) to listOf(
					SyntaxDefinitionSection(Token(LF), listOf(Token(string("\r")))),
					SyntaxDefinitionSection(Token(LF), listOf(Token(string("\n")))),
					SyntaxDefinitionSection(Token(LF), listOf(Token(string("\r\n"))))
			),
			Token(SEMI) to listOf(
					SyntaxDefinitionSection(Token(SEMI), listOf(Token(LF))),
					SyntaxDefinitionSection(Token(SEMI), listOf(Token(string(";")))),
					SyntaxDefinitionSection(Token(SEMI), listOf(Token(SEMI), Token(LF))),
					SyntaxDefinitionSection(Token(SEMI), listOf(Token(SEMI), Token(string(";"))))
			),

			Token(`SimpleName$with$stringLiteral$$,`) to listOf(
					SyntaxDefinitionSection(Token(`SimpleName$with$stringLiteral$$,`), listOf(Token(SimpleName))),
					SyntaxDefinitionSection(Token(`SimpleName$with$stringLiteral$$,`), listOf(Token(`SimpleName$with$stringLiteral$$,`), Token(string(",")), Token(SimpleName)))
			),

			Token(`SimpleName$with$stringLiteral$$dot`) to listOf(
					SyntaxDefinitionSection(Token(`SimpleName$with$stringLiteral$$dot`), listOf(Token(SimpleName))),
					SyntaxDefinitionSection(Token(`SimpleName$with$stringLiteral$$dot`), listOf(Token(`SimpleName$with$stringLiteral$$dot`), Token(string(".")), Token(SimpleName)))
			),

			Token(`PatternElements$with$stringLiteral$$colon`) to listOf(
					SyntaxDefinitionSection(Token(`PatternElements$with$stringLiteral$$colon`), listOf(Token(PatternElements))),
					SyntaxDefinitionSection(Token(`PatternElements$with$stringLiteral$$colon`), listOf(Token(`PatternElements$with$stringLiteral$$colon`), Token(string(":")), Token(PatternElements)))
			),

			Token(`PatternElements$with$stringLiteral$$|`) to listOf(
					SyntaxDefinitionSection(Token(`PatternElements$with$stringLiteral$$|`), listOf(Token(PatternElements))),
					SyntaxDefinitionSection(Token(`PatternElements$with$stringLiteral$$|`), listOf(Token(`PatternElements$with$stringLiteral$$|`), Token(string("|")), Token(PatternElements)))
			),

			Token(`SEMI?`) to listOf(
					SyntaxDefinitionSection(Token(`SEMI?`), listOf()),
					SyntaxDefinitionSection(Token(`SEMI?`), listOf(Token(SEMI)))
			),

			Token(`patType?`) to listOf(
					SyntaxDefinitionSection(Token(`patType?`), listOf()),
					SyntaxDefinitionSection(Token(`patType?`), listOf(Token(patType)))
			),

			Token(`PatternExp*`) to listOf(
					SyntaxDefinitionSection(Token(`PatternExp*`), listOf()),
					SyntaxDefinitionSection(Token(`PatternExp*`), listOf(Token(`PatternExp*`), Token(PatternExp)))
			),

			Token(`TopLebelObject*`) to listOf(
					SyntaxDefinitionSection(Token(`TopLebelObject*`), listOf()),
					SyntaxDefinitionSection(Token(`TopLebelObject*`), listOf(Token(`TopLebelObject*`), Token(TopLebelObject)))
			)
	)

	@Test
	fun generateSyntaxDB() {
		assertThat(generateSyntaxDB(syntaxDefinitions), `is`(expectedSyntaxDB))
	}

	val expectedClosureItemSet by lazy {
		ClosureSet(setOf(
				ClosureItem(SyntaxDefinitionSection(Token.Syntax, listOf(Token(kptFile))), 0, setOf(Token.Eof)),
				ClosureItem(SyntaxDefinitionSection(Token(kptFile), listOf(Token(`TopLebelObject*`))), 0, setOf(Token.Eof)),
				ClosureItem(SyntaxDefinitionSection(Token(`TopLebelObject*`), listOf()), 0, setOf(Token.Eof, Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package")))),
				ClosureItem(SyntaxDefinitionSection(Token(`TopLebelObject*`), listOf(Token(`TopLebelObject*`), Token(TopLebelObject))), 0, setOf(Token(SimpleName), Token(string("@skip")), Token(string("@import")), Token(string("@package")), Token.Eof))
		))
	}

	@Test
	fun expendClosureItemSet() {
		setEveryTokenAndDefinitionId(syntaxDefinitions)
		expectedClosureItemSet.set.forEach {
			it.syntax.ltoken.setIdAndTypeFromMap()
			it.syntax.pattern.forEach(Token::setIdAndTypeFromMap)
			it.lookaheads.forEach(Token::setIdAndTypeFromMap)
		}
		assertThat(expendClosureItemSet(setOf(initClosureItem), expectedSyntaxDB, expectedFirstMap), `is`(expectedClosureItemSet))
	}

	val grammarDefinition = GrammarDefinition(kptFile) {
		kptFile.pattern(`TopLebelObject*`)

		TopLebelObject.pattern(importFile)
		TopLebelObject.pattern(pattern)
		TopLebelObject.pattern(skip)
		TopLebelObject.pattern(importPackage)
		TopLebelObject.pattern(`package`)

		`package`.pattern(string("@package"), `SimpleName$with$stringLiteral$$dot`)

		importFile.pattern(string("@import"), string("("), string, string(")"))

		importPackage.pattern(string("@import"), `SimpleName$with$stringLiteral$$dot`)
		importPackage.pattern(string("@import"), `SimpleName$with$stringLiteral$$dot`, string("."), string("*"))
		importPackage.pattern(string("@import"), `SimpleName$with$stringLiteral$$dot`, string("as"), SimpleName)

		skip.pattern(string("@skip"), `SimpleName$with$stringLiteral$$,`)

		patType.pattern(string(":"), typeKt)

		pattern.pattern(SimpleName, `patType?`, SEMI, string("="), orPatterns, string(";"))
		pattern.pattern(SimpleName, `patType?`, SEMI, patterns, string(";"))

		orPatterns.pattern(`PatternElements$with$stringLiteral$$|`)

		patterns.pattern(string(":"), `PatternElements$with$stringLiteral$$colon`)

		PatternElements.pattern(`PatternExp*`)

		PatternExp.pattern(PatternElement)
		PatternExp.pattern(PatternElement, string("*"))
		PatternExp.pattern(PatternElement, string("+"))
		PatternExp.pattern(PatternElement, string("?"))
		PatternExp.pattern(PatternElement, string("{"), PatternElement, string("}"))

		PatternElement.pattern(string)
		PatternElement.pattern(SimpleName)

		typeKt.pattern(typeReference)

		typeReference.pattern(string("("), typeReference, string(")"))
		typeReference.pattern(userType)
		typeReference.pattern(nullableType)

		nullableType.pattern(typeReference, string("?"))

		userType.pattern(simpleUserType)
		userType.pattern(userType, string("."), simpleUserType)

		simpleUserType.pattern(SimpleName)
		simpleUserType.pattern(SimpleName, string("<"), projectionTypeWithConnma, string(">"))

		projectionType.pattern(projection, typeKt)
		projectionType.pattern(string("*"))

		projectionTypeWithConnma.pattern(projectionType)
		projectionTypeWithConnma.pattern(projectionTypeWithConnma, string(","), projectionType)

		projection.pattern()
		projection.pattern(varianceAnnotation)

		varianceAnnotation.pattern(string("in"))
		varianceAnnotation.pattern(string("out"))


		LF.pattern(string("\r"))
		LF.pattern(string("\n"))
		LF.pattern(string("\r\n"))
		SEMI.pattern(LF)
		SEMI.pattern(string(";"))
		SEMI.pattern(SEMI, LF)
		SEMI.pattern(SEMI, string(";"))

		`SimpleName$with$stringLiteral$$,`.pattern(SimpleName)
		`SimpleName$with$stringLiteral$$,`.pattern(`SimpleName$with$stringLiteral$$,`, string(","), SimpleName)

		`SimpleName$with$stringLiteral$$dot`.pattern(SimpleName)
		`SimpleName$with$stringLiteral$$dot`.pattern(`SimpleName$with$stringLiteral$$dot`, string("."), SimpleName)

		`PatternElements$with$stringLiteral$$colon`.pattern(PatternElements)
		`PatternElements$with$stringLiteral$$colon`.pattern(`PatternElements$with$stringLiteral$$colon`, string(":"), PatternElements)

		`PatternElements$with$stringLiteral$$|`.pattern(PatternElements)
		`PatternElements$with$stringLiteral$$|`.pattern(`PatternElements$with$stringLiteral$$|`, string("|"), PatternElements)

		`SEMI?`.pattern()
		`SEMI?`.pattern(SEMI)

		`patType?`.pattern()
		`patType?`.pattern(patType)

		`PatternExp*`.pattern()
		`PatternExp*`.pattern(`PatternExp*`, PatternExp)

		`TopLebelObject*`.pattern()
		`TopLebelObject*`.pattern(`TopLebelObject*`, TopLebelObject)
	}
	val initSyntaxDefinitionSection = SyntaxDefinitionSection(Token.Syntax, listOf(grammarDefinition.start_symbol))
	val initClosureItem = ClosureItem(initSyntaxDefinitionSection, 0, setOf(Token.Eof))

	val syntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()
			.also { it.add(initSyntaxDefinitionSection) }
			.also { it.addAll(grammarDefinition.syntax) }
}

fun GrammarDefinition(startToken: String, block: GrammarDefinitionBuilder.()->Unit): GrammarDefinition {
	val builder = GrammarDefinitionBuilder(Token(startToken))
	builder.block()
	return builder.build()
}

class GrammarDefinitionBuilder(val startToken: Token){
	private val syntaxDefinitions: MutableList<SyntaxDefinitionSectionBuilder> = mutableListOf()

	fun String.pattern(vararg pattern: String): SyntaxDefinitionSectionBuilder {
		val builder = SyntaxDefinitionSectionBuilder(Token(this), pattern.map { Token(it) })
		syntaxDefinitions.add(builder)
		return builder
	}

	internal fun build() = GrammarDefinition(syntaxDefinitions.map { it.build() }, startToken)
}

class SyntaxDefinitionSectionBuilder (val ltoken: Token, val pattern: List<Token>) {
	fun build() = SyntaxDefinitionSection(ltoken, pattern)
}