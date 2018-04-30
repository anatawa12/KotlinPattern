package com.anatawa12.parser.frontend

import com.anatawa12.libs.util.unescape
import com.anatawa12.parser.parser.Token

/**
 * Created by anatawa12 on 2018/04/15.
 */

class Lexer(val input: String) {
	var index = 0

	fun lex(): Token {
		index += spaceNoLF.find(input, index)?.value?.length ?: 0
		if (index == input.length) return Token.Eof

		val SimpleName = SimpleName.find(input, index)
		val keyworlds = keyworlds.find(input, index)
		val string = string.find(input, index)
		var result: MatchResult? = null
		var max = -1
		if (SimpleName != null && SimpleName.range.first == index && SimpleName.value.length > max) {
			max = SimpleName.value.length
			result = SimpleName
		}
		if (keyworlds != null && keyworlds.range.first == index && keyworlds.value.length > max) {
			max = keyworlds.value.length
			result = keyworlds
		}
		if (string != null && string.range.first == index && string.value.length > max) {
			max = string.value.length
			result = string
		}
		if (result == null) error("not match string: ${input.substring(index).lineSequence().iterator().next()}\n\tat ${errorAt()}\n\tat $index ${input.length}")

		index += max

		return when (result) {
			SimpleName -> Token("SimpleName", result.value)
			string -> Token("string", result.groupValues[1].unescape())
			else -> Token(string(result.value), result.value)
		}
	}

	fun errorAt(): Pair<Int, Int>{
		val parsed = input.substring(0, index)
		val lines = parsed.lines()
		return lines.size to lines.last().length
	}

	companion object {
		val spaceNoLF = """[\s&&[^\r\n]]*""".toRegex()
		val string = """\"(([^\"\\\n\t\r\f]|\\u[0-9A-Za-z]{4}|\\[bnrt\\])*?)\"""".toRegex()
		val SimpleName = """\p{javaLetter}\p{javaLetterOrDigit}*""".toRegex()
		val keyworlds = "@package|@import|@skip|in|out|as|[(){}=;:*+?|.,<>]|\n|\r|\r\n".toRegex()
	}
}
