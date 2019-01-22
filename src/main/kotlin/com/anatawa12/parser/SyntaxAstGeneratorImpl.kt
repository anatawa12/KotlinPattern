package com.anatawa12.parser.frontend

import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.frontend.generated.ISyntaxAstGenerator
import com.anatawa12.parser.frontend.generated.Token

/**
 * Created by anatawa12 on 2018/05/03.
 */
object SyntaxAstGeneratorImpl : ISyntaxAstGenerator {
	override fun kptFile_0(arg0: MutableList<TopLevelObject>): Kpt = Kpt(arg0)

	override fun package_0(arg1: MutableList<Token>): Package = Package(arg1.joinToString(separator = ".") { it.data })

	override fun TopLebelObject_0(arg0: Package): TopLevelObject = arg0

	override fun TopLebelObject_1(arg0: TopLevelObject): TopLevelObject = arg0

	override fun TopLebelObject_2(arg0: TopLevelObject): TopLevelObject = arg0

	override fun TopLebelObject_3(arg0: TopLevelObject): TopLevelObject = arg0

	override fun TopLebelObject_4(arg0: TopLevelObject): TopLevelObject = arg0

	override fun TopLebelObject_5(arg0: TopLevelObject): TopLevelObject = arg0

	override fun importFile_0(arg2: Token): TopLevelObject = ImportFile(from = arg2.data)

	override fun importPackage_0(arg1: MutableList<Token>): TopLevelObject = ImportPackeage(arg1.joinToString(separator = ".") { it.data })

	override fun importPackage_1(arg1: MutableList<Token>): TopLevelObject = ImportPackeage(arg1.joinToString(separator = ".", postfix = ".*") { it.data })

	override fun importPackage_2(arg1: MutableList<Token>, arg3: Token): TopLevelObject = ImportPackeage(arg1.joinToString(separator = ".", postfix = "as ${arg3.data}") { it.data })

	override fun tokenName_0(arg0: Token, arg2: Token): OfTokenName = error("")

	override fun stringName_0(arg1: Token, arg2: Token): TopLevelObject = StringName(arg1.data, arg2.data)

	override fun skip_0(arg1: MutableList<Token>): TopLevelObject = Skip(arg1.map(Token::data))

	override fun patType_0(arg1: String): String = arg1

	override fun pattern_0(arg0: Token, arg1: String?, arg4: List<PatternElements>): TopLevelObject = Pattern(arg1?:"Unit", arg0.data, arg4)

	override fun pattern_1(arg0: Token, arg1: String?, arg3: List<PatternElements>): TopLevelObject  = Pattern(arg1?:"Unit", arg0.data, arg3)

	override fun orPatterns_0(arg0: MutableList<PatternElements>): List<PatternElements> = arg0

	override fun patterns_0(arg0: Token, arg1: MutableList<PatternElements>): List<PatternElements> = arg1

	override fun PatternElements_0(arg0: MutableList<PatternExp>): PatternElements = arg0

	override fun PatternExp_0(arg0: PatternElement): PatternExp = PatternElementExp(arg0)

	override fun PatternExp_1(arg0: PatternElement): PatternExp = PatternElementZeroMoreExp(arg0)

	override fun PatternExp_2(arg0: PatternElement): PatternExp = PatternElementOneMoreExp(arg0)

	override fun PatternExp_3(arg0: PatternElement): PatternExp = PatternElementOptionalExp(arg0)

	override fun PatternExp_4(arg0: PatternElement, arg2: PatternElement): PatternExp = PatternElementWithSepExp(arg0, arg2)

	override fun PatternExp_5(arg0: Token, arg1: PatternElement): PatternExp = PatternElementExp(arg1, true)

	override fun PatternExp_6(arg0: Token, arg1: PatternElement): PatternExp = PatternElementZeroMoreExp(arg1, true)

	override fun PatternExp_7(arg0: Token, arg1: PatternElement): PatternExp = PatternElementOneMoreExp(arg1, true)

	override fun PatternExp_8(arg0: Token, arg1: PatternElement): PatternExp = PatternElementOptionalExp(arg1, true)

	override fun PatternExp_9(arg0: Token, arg1: PatternElement, arg3: PatternElement): PatternExp = PatternElementWithSepExp(arg1, arg3, true)

	override fun PatternElement_0(arg0: Token): PatternElement = StringElement(arg0.data)

	override fun PatternElement_1(arg0: Token): PatternElement = TokenElement(arg0.data)

	override fun typeKt_0(arg0: String): String = arg0

	override fun typeReference_0(arg1: String): String = "($arg1)"

	override fun typeReference_1(arg0: String): String = arg0

	override fun typeReference_2(arg0: String): String = arg0

	override fun nullableType_0(arg0: String): String = "$arg0?"

	override fun userType_0(arg0: String): String = arg0

	override fun userType_1(arg0: String, arg2: String): String = "$arg0.$arg2"

	override fun simpleUserType_0(arg0: Token): String = arg0.data

	override fun simpleUserType_1(arg0: Token, arg2: MutableList<String>): String = "${arg0.data}<${arg2.joinToString()}>"

	override fun projectionType_0(arg0: String?, arg1: String): String = "${arg0.orEmpty()}$arg1"

	override fun projectionType_1(): String = "*"

	override fun projection_0(arg0: String): String = arg0

	override fun varianceAnnotation_0(arg0: Token): String = "in "

	override fun varianceAnnotation_1(arg0: Token): String = "out "

	override fun SEMI_0() {}

	override fun SEMI_1() {}

	override fun SEMI_2() {}

	override fun SEMI_3() {}

}
// */