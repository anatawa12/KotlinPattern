package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.synchronized.synchronizedMap
import com.anatawa12.libs.util.escape
import kotlin.properties.Delegates

/**
 * Created by anatawa12 on 2018/04/15.
 */
open class Token constructor(val name: String, val data: String? = null){

	var runTimeId: Int by Delegates.notNull()
	var resultId: Int by Delegates.notNull()
	lateinit var type: TokenType

	fun setIdAndTypeFromMap() {
		map[this]?.also { (id, type) ->
			resultId = id
			runTimeId = id + 1
			this.type = type
		}
	}

	init {
		setIdAndTypeFromMap()
	}

	override fun equals(other: Any?): Boolean {
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
		var map = mapOf<Token, Pair<Int, TokenType>>().synchronizedMap()
		val Eof = Token("EOF")
		val Syntax = Token("S'")
	}
}


