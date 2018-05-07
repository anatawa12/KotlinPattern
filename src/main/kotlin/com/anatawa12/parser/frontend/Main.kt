package com.anatawa12.parser.frontend

import com.anatawa12.parser.frontend.generated.SyntaxRunner
import com.anatawa12.parser.parser.ReduceReduceConflictException
import kotlinx.coroutines.experimental.runBlocking
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

fun string(string: String) = "stringLiteral$$$string"

@Throws(ReduceReduceConflictException::class)
fun kotlinPatten(args: KotlinPatternArguments) = runBlocking {
	println(args)
	val lexer = Lexer(args.input.readText())
	val runner = SyntaxRunner(lexer::lex, SyntaxAstGeneratorImpl)
	runner.run()

	val (src, packageName)= SyntaxDefectionGenerator.generate(runner.result, args)

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

fun main(args: Array<String>) {
	fun error(message: String): Nothing {
		System.err.println(message)
		exitProcess(1)
	}
	var outPutType: OutputType? = null
	var input: File? = null
	var output: File? = null
	var canReadOption = true
	var seeLookaheadsOnError = false
	var curIndex = 0
	while (curIndex in args.indices) {
		val arg = args[curIndex++]
		if (canReadOption && arg.startsWith("-")) {
			if (arg.startsWith("--")) {
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
					"--see-lookaheads" -> {
						seeLookaheadsOnError = true
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

	try {
		kotlinPatten(KotlinPatternArguments(input, output, outPutType, seeLookaheadsOnError))
	} catch (ex: ReduceReduceConflictException) {
		System.exit(1)
	}
}

data class KotlinPatternArguments(val input: File, val output: File, val outPutType: OutputType, val seeLookaheadsOnError: Boolean)

enum class OutputType {
	PackageRootDir,
	FilePath
}
