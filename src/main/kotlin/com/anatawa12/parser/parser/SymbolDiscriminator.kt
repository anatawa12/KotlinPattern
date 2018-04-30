package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.InitMutableMap
import com.anatawa12.parser.logging.Section

/**
 * Created by anatawa12 on 2018/04/15.
 */
class SymbolDiscriminator(syntaxdef: SyntaxDefinitions) {
	var terminalSymbols = mutableSetOf<Token>()
		private set
	var nonterminalSymbols = mutableSetOf<Token>()
		private set

	init {
		for (sect in syntaxdef) {
			val symbol = sect.ltoken
			nonterminalSymbols.add(symbol)
		}

		for (sect in syntaxdef) {
			for (symbol in sect.pattern) {
				if (symbol !in nonterminalSymbols) {
					terminalSymbols.add(symbol)
				}
			}
		}
	}
}

class NullableSet (private val syntax: SyntaxDefinitions, private val nulls: MutableSet<Token> = mutableSetOf()): Set<Token> by nulls {
	init {
		for (rule in syntax) {
			if (rule.pattern.size == 0) {
				nulls.add(rule.ltoken)
			}
		}

		do {
			var changed = false
			for (rule in syntax) {
				if (rule.ltoken in nulls) continue;
				if (rule.pattern.all { it in nulls }){
					changed = true;
					nulls.add(rule.ltoken)
				}
			}
		} while (changed)
	}
}

class FirstSet (syntax: SyntaxDefinitions, symbols: SymbolDiscriminator) {
	private val firstMap: Map<Token, Set<Token>>
	private val nulls = NullableSet(syntax)
	init {
		Section.start("make FirstSet")
		val firstResult = InitMutableMap<Token, MutableSet<Token>>(::mutableSetOf)
		//($) = $ だけ手動で設定
		firstResult[Token.Eof] = mutableSetOf(Token.Eof)

		// 終端記号にたいして X = X
		symbols.terminalSymbols.forEach { token ->
			firstResult[token] = mutableSetOf(token)
		}

		val constaint = mutableListOf<Pair<Token, Token>>()
		for (rule in syntax) {
			val sup = rule.ltoken
			for (sub in rule.pattern) {
				if(sup != sub)
					constaint += sup to sub
				if(sub !in nulls)
					break
			}
		}

		do {
			var changed = false;
			for ((sup, sub) in constaint) {
				val superSet = firstResult[sup]
				val subSet = firstResult[sub]

				subSet.forEach { token ->
					if (token !in superSet) {
						superSet.add(token)
						changed = true
					}
				}
			}
		} while(changed)
		firstMap = firstResult
		Section.end()
	}

	operator fun get(arg: Token): Set<Token> = firstMap[arg] ?: error("invaid token found: $arg")

	operator fun get(tokens: Iterable<Token>): Set<Token> {
		val result = mutableSetOf<Token>()
		for (token in tokens) {
			val add = firstMap[token] ?: error("invaid token found: $token")
			add.forEach { token ->
				result.add(token)
			}
			if (token !in nulls)
				break
		}
		return result
	}
}

