package com.anatawa12.parser.frontend

import com.anatawa12.libs.util.escape
import com.anatawa12.parser.frontend.ast.*
import com.anatawa12.parser.logging.Log
import com.anatawa12.parser.parser.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Created by anatawa12 on 2018/01/12.
 */

val SimpleName = "SimpleName"
val string = "string"

val kptFile = "kptFile"
val TopLebelObject = "TopLevelObject"
val `package` = "`package`"
val patType = "patType"
val importFile = "importFile"
val importPackage = "importPackage"
val skip = "skip"

val pattern = "pattern"
val SEMI = "SEMI"
val typeKt = "typeKt"
val orPatterns = "orPatterns"
val patterns = "patterns"
val PatternElements = "PatternElements"
val PatternExp = "PatternExp"
val PatternElement = "PatternElement"
val LF = "LF"
val typeReference = "typeReference"
val userType = "userType"
val simpleUserType = "simpleUserType"
val projection = "projection"
val varianceAnnotation = "varianceAnnotation"
val projectionType = "projectionType"
val projectionTypeWithConnma = "projectionTypeWithConnma"
val nullableType = "nullableType"

val `TopLebelObject*` = "TopLevelObject*"
val `PatternExp*` = "PatternExp*"

val `SEMI?` = "SEMI?"
val `patType?` = "patType?"

val `SimpleName$with$stringLiteral$$,` = "SimpleName\$with\$stringLiteral\$\$,"
val `SimpleName$with$stringLiteral$$dot` = "SimpleName\$with\$stringLiteral\$$."
val `PatternElements$with$stringLiteral$$|` = "PatternElements\$with\$stringLiteral\$\$|"
val `PatternElements$with$stringLiteral$$colon` = "PatternElements\$with\$stringLiteral\$\$:"

lateinit var generator: ParsingTableGenerator

fun string(string: String) = "stringLiteral$$$string"
fun kotlinPatten(args: KotlinPatternArgments){
	println(args)
	//System.setOut(MultiPrintStream(File("./Log.log"), System.out))

	val definition = GrammarDefinition(kptFile) {
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
	//println(definition)
	generator = GenerateParsingTable(definition)

	//val lexer = Lexer(System.`in`.bufferedReader().readText())
	val lexer = Lexer(args.input.readText())

	val runner = SyntaxRunnner(generator, lexer::lex, setOf(Token(string("\r")), Token(string("\n")), Token(string("\r\n"))), ::ast, lexer::errorAt)
	runner.run()
	Log.debug(runner.resultSyntaxes.map { generator.syntax[it] })
	Log.debug(runner.result)

	val (src, packageName)= SyntaxDefectionGenerator.generate(runner.result as Kpt)

	when (args.outPutType) {
		OutputType.PackageRootDir -> {
			val dir = File(args.output, packageName.replace('.', File.separatorChar)).also { it.mkdirs() }
			File(dir, "Parser.kt").outputStream().write(src.toByteArray())
		}
		OutputType.FilePath -> {
			File(args.output, "..").mkdirs()
			if (!args.output.exists()) {
				args.output.createNewFile()
			}
			args.output.outputStream().write(src.toByteArray())
		}
	}
}

@Suppress("UNCHECKED_CAST")
fun ast(inPattenToken: Token, list: List<Any?>): Any? {

	//fun string(string: String) = "stringLiteral$$$string"
	when (inPattenToken.name) {
		kptFile -> {
			return Kpt(list[0] as List<TopLevelObject>)
		}
		TopLebelObject -> {
			return list[0]
		}
		`package` -> {
			return Package((list[1] as List<Token>).joinToString(separator = "."){it.data!!})
		}
		importFile -> {
			return ImportFile((list[2] as Token).data!!)
		}
		importPackage -> {
			return if (list.size == 2) {
				ImportPackeage((list[1] as List<Token>).joinToString(separator = "."){it.data!!})
			} else if((list[3] as Token).name == SimpleName){
				ImportPackeage((list[1] as List<Token>).joinToString(separator = ".", postfix = " as ${(list[3] as Token).data}"){it.data!!})
			} else {
				ImportPackeage((list[1] as List<Token>).joinToString(separator = ".", postfix = ".*"){it.data!!})
			}
		}
		skip -> {
			return Skip((list[1] as List<Token>).map { it.data!! } )
		}
		patType -> {
			return list[1] as String
		}
		pattern -> {
			return if(list.size == 6){
				Pattern((list[1] as String?) ?: "Unit", (list[0] as Token).data!!, list[4] as List<PatternElements>)
			} else {
				Pattern((list[1] as String?) ?: "Unit", (list[0] as Token).data!!, list[3] as List<PatternElements>)
			}
		}
		SEMI -> {
			return null
		}
		typeKt -> {
			return list[0] as String
		}
		orPatterns -> {
			return list[0] as List<PatternElements>
		}
		patterns -> {
			return list[1] as List<PatternElements>
		}
		PatternElements -> {
			return list[0] as PatternElements
		}
		PatternExp -> {
			val elem = list[0] as PatternElement
			return when(list.size) {
				1 -> PatternElementExp(elem)
				2 -> when ((list[1] as Token).name) {
					string("*") -> PatternElementZeroMoreExp(elem)
					string("+") -> PatternElementOneMoreExp(elem)
					string("?") -> PatternElementOptionalExp(elem)
					else -> error("")
				}
				else -> PatternElementWithSepExp(elem, list[2] as PatternElement)
			}
		}
		PatternElement -> {
			val token = list[0] as Token
			return when (token.name) {
				"string" -> StringElement(token.data!!.escape())
				else -> TokenElement(token.data!!)
			}
		}
		LF -> {
			return null
		}
		typeReference -> {
			return when(list.size) {
				1 -> list[0] as String
				else -> "(${list[0] as String})"
			}
		}
		nullableType -> {
			return "${list[0] as String}?"
		}
		userType -> {
			return when(list.size) {
				1 -> list[0] as String
				else -> "${list[0] as String}.${list[2] as String}"
			}
		}
		simpleUserType -> {
			return when(list.size) {
				1 -> (list[0] as Token).data
				else -> "${(list[0] as Token).data}<${list[2] as String}>"
			}
		}
		projection -> {
			return when(list.size) {
				0 -> ""
				else -> "${list[0] as String} "
			}
		}
		varianceAnnotation -> {
			return (list[0] as Token).data
		}
		projectionType -> {
			return when(list.size) {
				1 -> "*"
				else -> "${list[0] as String}${list[1] as String}"
			}
		}
		projectionTypeWithConnma -> {
			return when(list.size) {
				1 -> list[0] as String
				else -> "${list[0] as String},${list[2] as String}"
			}
		}

		`TopLebelObject*`, `PatternExp*` -> {
			return when(list.size) {
				0 -> mutableListOf()
				else -> (list[0] as MutableList<Any?>).also { it.add(list[1]) }
			}
		}

		`SEMI?`, `patType?` -> {
			return when(list.size) {
				0 -> null
				else -> list[0]
			}
		}
		`SimpleName$with$stringLiteral$$dot`, `SimpleName$with$stringLiteral$$,`, `PatternElements$with$stringLiteral$$|`, `PatternElements$with$stringLiteral$$colon` -> {
			return when(list.size) {
				1 -> mutableListOf(list[0])
				else -> (list[0] as MutableList<Any?>).also { it.add(list[2]) }
			}
		}
	}
	error(inPattenToken)
}

fun listLexer(vararg tokens: Token): () -> Token {
	var index = 0
	return {
		//println("lex $index: ${tokens[index]}")
		tokens[index++]
	}
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

fun main(args: Array<String>) {
	fun error(message: String): Nothing {
		System.err.println(message)
		exitProcess(1)
	}
	var outPutType: OutputType? = null
	var input: File? = null
	var output: File? = null
	var canReadOption = true
	var curIndex = 0
	while (curIndex in args.indices) {
		val arg = args[curIndex++]
		if (canReadOption && arg.startsWith("-")) {
			if (canReadOption && arg.startsWith("--")) {
				when (arg) {
					"--" -> canReadOption = false
					"--package-root" -> {
						if (outPutType != null) error("you can't set multiple out file/dir type.")
						outPutType = OutputType.PackageRootDir
					}
					"--file-path" -> {
						if (outPutType != null) error("you can't set multiple out file/dir type.")
						outPutType = OutputType.FilePath
					}
					"--output" -> {
						if (output != null) error("you can't set multiple out file/dir.")
						if (curIndex in args.indices) error("must have a argument for $arg option.")
						output = File(args[curIndex++])
					}
					"--input" -> {
						if (input != null) error("you can't set multiple in file/dir.")
						if (curIndex in args.indices) error("must have a argument for $arg option.")
						input = File(args[curIndex++])
					}
					else -> error("$arg is invalid option")
				}
			} else {
				var curCharIndex = 1
				while (curCharIndex < arg.length) {
					val argChar = arg[curCharIndex++]
					when (argChar) {
						'p' -> {
							if (outPutType != null) error("you can't set multiple out file/dir type.")
							outPutType = OutputType.PackageRootDir
						}
						'f' -> {
							if (outPutType != null) error("you can't set multiple out file/dir type.")
							outPutType = OutputType.FilePath
						}
						'o' -> {
							if (output != null) error("you can't set multiple out file/dir.")
							if (curIndex in args.indices) error("must have a argument for o option.")
							output = File(args[curIndex++])
						}
						'i' -> {
							if (input != null) error("you can't set multiple in file/dir.")
							if (curIndex in args.indices) error("must have a argument for i option.")
							input = File(args[curIndex++])
						}
						else -> error("$argChar is invalid option")
					}
				}
			}
		} else {
			curIndex--
			break
		}
	}
	outPutType = outPutType ?: OutputType.FilePath

	if (input == null) {
		if (curIndex !in args.indices) error("there is no input.")
		input = File(args[curIndex++])
	}

	if (output == null) {
		if (curIndex !in args.indices) error("there is no output.")
		output = File(args[curIndex++])
	}

	kotlinPatten(KotlinPatternArgments(input, output, outPutType))
}

data class KotlinPatternArgments(val input: File, val output: File, val outPutType: OutputType)

enum class OutputType {
	PackageRootDir,
	FilePath
}
