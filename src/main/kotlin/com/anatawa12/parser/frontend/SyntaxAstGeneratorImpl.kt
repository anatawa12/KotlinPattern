package com.anatawa12.parser.frontend

import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.frontend.generated.SyntaxAstGenerator
import com.anatawa12.parser.frontend.generated.Token

/**
 * Created by anatawa12 on 2018/05/03.
 */
object SyntaxAstGeneratorImpl : SyntaxAstGenerator {

	override fun kptFile_0(`TopLebelObject*_0`: MutableList<TopLevelObject>): Kpt = Kpt(`TopLebelObject*_0`)

	override fun package_0(`"@package"_0`: Token, `SimpleName&"!d"_1`: MutableList<Token>): Package = Package(`SimpleName&"!d"_1`.joinToString(separator = ".") { it.data })

	override fun TopLebelObject_0(package_0: Package): TopLevelObject = package_0

	override fun TopLebelObject_1(importFile_0: TopLevelObject): TopLevelObject = importFile_0

	override fun TopLebelObject_2(pattern_0: TopLevelObject): TopLevelObject = pattern_0

	override fun TopLebelObject_3(skip_0: TopLevelObject): TopLevelObject = skip_0

	override fun TopLebelObject_4(importPackage_0: TopLevelObject): TopLevelObject = importPackage_0

	override fun importFile_0(`"@import"_0`: Token, `"("_1`: Token, string_2: Token, `")"_3`: Token): TopLevelObject = ImportFile(from = string_2.data)

	override fun importPackage_0(`"@import"_0`: Token, `SimpleName&"!d"_1`: MutableList<Token>): TopLevelObject = ImportPackeage(`SimpleName&"!d"_1`.joinToString(separator = ".") { it.data })

	override fun importPackage_1(`"@import"_0`: Token, `SimpleName&"!d"_1`: MutableList<Token>, `"!d"_2`: Token, `"*"_3`: Token): TopLevelObject = ImportPackeage(`SimpleName&"!d"_1`.joinToString(separator = ".", postfix = ".*") { it.data })

	override fun importPackage_2(`"@import"_0`: Token, `SimpleName&"!d"_1`: MutableList<Token>, `"as"_2`: Token, SimpleName_3: Token): TopLevelObject = ImportPackeage(`SimpleName&"!d"_1`.joinToString(separator = ".", postfix = "as ${SimpleName_3.data}") { it.data })

	override fun tokenName_0(SimpleName_0: Token, `"!la"_1`: Token, SimpleName_2: Token, `"!ra"_3`: Token): OfTokenName = OfTokenName(SimpleName_0.data, SimpleName_2.data)

	override fun skip_0(`"@skip"_0`: Token, `SimpleName&","_1`: MutableList<Token>): TopLevelObject = Skip(`SimpleName&","_1`.map(Token::data))

	override fun patType_0(`"!c"_0`: Token, typeKt_1: String): String = typeKt_1

	override fun pattern_0(SimpleName_0: Token, `patType?_1`: String?, SEMI_2: Unit, `"="_3`: Token, orPatterns_4: List<PatternElements>, `"!s"_5`: Token): TopLevelObject = Pattern(`patType?_1`?:"Unit", SimpleName_0.data, orPatterns_4)

	override fun pattern_1(SimpleName_0: Token, `patType?_1`: String?, SEMI_2: Unit, patterns_3: List<PatternElements>, `"!s"_4`: Token): TopLevelObject  = Pattern(`patType?_1`?:"Unit", SimpleName_0.data, patterns_3)

	override fun orPatterns_0(`PatternElements&"|"_0`: MutableList<PatternElements>): List<PatternElements> = `PatternElements&"|"_0`

	override fun patterns_0(`"!c"_0`: Token, `PatternElements&"!c"_1`: MutableList<PatternElements>): List<PatternElements> = `PatternElements&"!c"_1`

	override fun PatternElements_0(`PatternExp*_0`: MutableList<PatternExp>): PatternElements = `PatternExp*_0`

	override fun PatternExp_0(PatternElement_0: PatternElement): PatternExp = PatternElementExp(PatternElement_0)

	override fun PatternExp_1(PatternElement_0: PatternElement, `"*"_1`: Token): PatternExp = PatternElementZeroMoreExp(PatternElement_0)

	override fun PatternExp_2(PatternElement_0: PatternElement, `"+"_1`: Token): PatternExp = PatternElementOneMoreExp(PatternElement_0)

	override fun PatternExp_3(PatternElement_0: PatternElement, `"?"_1`: Token): PatternExp = PatternElementOptionalExp(PatternElement_0)

	override fun PatternExp_4(PatternElement_0: PatternElement, `"{"_1`: Token, PatternElement_2: PatternElement, `"}"_3`: Token): PatternExp = PatternElementWithSepExp(PatternElement_0, PatternElement_2)

	override fun PatternElement_0(string_0: Token): PatternElement = StringElement(string_0.data)

	override fun PatternElement_1(SimpleName_0: Token): PatternElement = TokenElement(SimpleName_0.data)

	override fun typeKt_0(typeReference_0: String): String = typeReference_0

	override fun typeReference_0(`"("_0`: Token, typeReference_1: String, `")"_2`: Token): String = "($typeReference_1)"

	override fun typeReference_1(userType_0: String): String = userType_0

	override fun typeReference_2(nullableType_0: String): String = nullableType_0

	override fun nullableType_0(typeReference_0: String, `"?"_1`: Token): String = "$typeReference_0?"

	override fun userType_0(simpleUserType_0: String): String = simpleUserType_0

	override fun userType_1(userType_0: String, `"!d"_1`: Token, simpleUserType_2: String): String = "$userType_0.$simpleUserType_2"

	override fun simpleUserType_0(SimpleName_0: Token): String = SimpleName_0.data

	override fun simpleUserType_1(SimpleName_0: Token, `"!la"_1`: Token, `projectionType&","_2`: MutableList<String>, `"!ra"_3`: Token): String = "${SimpleName_0.data}<${`projectionType&","_2`.joinToString()}>"

	override fun projectionType_0(`projection?_0`: String?, typeKt_1: String): String = "${`projection?_0`.orEmpty()}$typeKt_1"

	override fun projectionType_1(`"*"_0`: Token): String = "*"

	override fun projection_0(varianceAnnotation_0: String): String = varianceAnnotation_0

	override fun varianceAnnotation_0(`"in"_0`: Token): String = "in "

	override fun varianceAnnotation_1(`"out"_0`: Token): String = "out "

	override fun SEMI_0(LF_0: Token) {}

	override fun SEMI_1(`"!s"_0`: Token) {}

	override fun SEMI_2(SEMI_0: Unit, LF_1: Token) {}

	override fun SEMI_3(SEMI_0: Unit, `"!s"_1`: Token) {}

}
// */