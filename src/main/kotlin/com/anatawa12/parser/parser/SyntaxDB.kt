package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.InitMutableMap
import com.anatawa12.parser.logging.Section

/**
 * Created by anatawa12 on 2018/04/15.
 */
class SyntaxDB(grammar: GrammarDefinition) {
	private val syntax = grammar.syntax
	val startSymbol = grammar.start_symbol
	//private set
	val symbols = SymbolDiscriminator(grammar.syntax)
	//private set
	val first = FirstSet(syntax, symbols)
	//private set
	private var tokenidCounter = 0
	private val tokenMap = mutableMapOf<Token, Int>()
	private val defMap = InitMutableMap<Token, MutableList<Def>>(::mutableListOf)

	init {
		Section.start("make SyntaxDB")
		syntax.forEachIndexed { i, s ->
			val tmp = defMap[s.ltoken]
			tmp.add(Def(i, s))
			defMap[s.ltoken] = tmp
		}
		Section.end()
	}

	fun findDef(token: Token): List<Def> = defMap[token]

	fun getTokenId(token: Token) = tokenMap.getOrPut(token) { tokenidCounter++ }

	operator fun get(id: Int): SyntaxDefinitionSection {
		if (id == -1) {
			return SyntaxDefinitionSection(Token.Syntax, listOf(startSymbol))
		}
		return syntax[id]
	}
}

data class Def (val id: Int, val def: SyntaxDefinitionSection)

