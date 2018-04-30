package com.anatawa12.parser.parser

import com.anatawa12.libs.util.escape

/**
 * Created by anatawa12 on 2018/04/15.
 */
data class Token constructor(val name: String, val data: String? = null){
	val varName: String by lazy {
		val varName = name.replace("!", "!!")
				.replace(".", "!d")//Dot
				.replace(";", "!s")//Semi
				.replace(":", "!c")//Colon
				.replace("[", "!ls")//Left Square bracket
				.replace("]", "!rs")//Right Square bracket
				.replace("<", "!la")//Left Angle bracket
				.replace(">", "!ra")//Right Angle bracket
				.replace("\\", "!h")//backslasH
				.replace("/", "!s")//Slash
				.replace("\b", "!b")
				.replace("\n", "!n")
				.replace("\r", "!r")
				.replace("\t", "!t")
		if (varName !in keywords && varName.matches("""^[\p{javaLetter}_][\p{javaLetterOrDigit}_]*$""".toRegex())) varName else "`$varName`"
	}
	//constructor(name: String) : this(name)

	override fun equals(other: Any?): Boolean {
		val nouse = ".;[]<>:\\/\b\n\r"
		if (other !is Token) return false
		if(this === Eof || this === Syntax)
			return this === other
		return name == other.name
	}

	override fun hashCode(): Int {
		return name.hashCode()
	}

	override fun toString(): String = if (data == null) "Token(${name.escape()})" else "Token(${name.escape()}, \"${data.escape()}\")"

	companion object {
		val Eof = Token("EOF")
		val Syntax = Token("S'")
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
	}
}