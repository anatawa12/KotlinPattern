package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.sortWith
import com.anatawa12.libs.util.escape
import com.anatawa12.parser.logging.Log
import com.anatawa12.parser.logging.section

/**
 * Created by anatawa12 on 2018/04/15.
 */
data class ClosureItem constructor(
		val syntax: SyntaxDB,
		val syntaxId: Int,
		val dotIndex: Int,
		val lookaheads: MutableList<Token>) {
	init {
		lookaheads.sortWith{ t1, t2 -> syntax.getTokenId(t1) - syntax.getTokenId(t2) }
	}
	val lr0Hash: String = "$syntaxId,$dotIndex"
	val lr1Hash: String = "$syntaxId,$dotIndex,${lookaheads.joinToString(separator=",", prefix="[", postfix="]") { syntax.getTokenId(it).toString() }}"

	fun isSameLr0(that: ClosureItem) = this.lr0Hash == that.lr0Hash

	fun isSameLr1(that: ClosureItem) = this.lr1Hash == that.lr1Hash
	val toString: String by lazy {

		val syntax = syntax[syntaxId]
		val ltoken = syntax.ltoken
		val pattern = syntax.pattern.mapTo(mutableListOf()) { "\"${it.name.escape()}\"" }
		pattern.add(dotIndex, ".")
		//"${ltoken.name} -> ${pattern.joinToString(separator = " "){"\"${it.name}\""}}"
		"ClosureItem(${ltoken.name.escape()} -> ${pattern.joinToString(separator = " ")} ,${lookaheads.joinToString(separator = ",", prefix = "[", postfix = "]")})"
	}
	override fun toString(): String = toString
}

class ClosureSet (private val syntax: SyntaxDB, var closureSet: MutableList<ClosureItem>, noExpend: Boolean = false) {
	val lr0Hash = closureSet.joinToString(separator="|", prefix="", postfix="") { it.lr0Hash }
	val lr1Hash = closureSet.joinToString(separator="|", prefix="", postfix="") { it.lr1Hash }
	init {
		if (!noExpend)expend()
	}

	fun expend () = section("ClosureSet expend"){
		fun MutableList<ClosureItem>.sort() { this.sortWith { i1, i2 -> i1.lr1Hash.compareTo(i2.lr1Hash) } }
		var tmpSet = mutableListOf<ClosureItem>()

		section("analyze") {
			for (ci in closureSet) {
				ci.lookaheads.mapTo(tmpSet) {
					ci.copy(syntax = syntax, lookaheads = mutableListOf(it))
							.also { Log.debug("$it") }
				}
			}
		}

		section("sort") {
			tmpSet = tmpSet.toSet().toMutableList()
			tmpSet.sort()
		}

		var i = 0
		while (i < tmpSet.size) {
			section("expend: ${tmpSet[i]}") {
				val ci = tmpSet[i]
				val (ltoken, pattern) = syntax[ci.syntaxId]
				//.が末尾
				if (ci.dotIndex == pattern.size) return@section
				val follow = pattern[ci.dotIndex]
				if (follow !in syntax.symbols.nonterminalSymbols) return@section
				val lookaheads = syntax.first[pattern.run { subList(ci.dotIndex + 1, size) }]
						.toMutableList()
						.also { it.add(ci.lookaheads[0]) }
				lookaheads.sortWith { t1, t2 -> syntax.getTokenId(t1) - syntax.getTokenId(t2) }

				val definitions = syntax.findDef(follow)
				for ((id, def) in definitions) {
					for (la in lookaheads) {
						val newCi = ClosureItem(syntax, id, 0, mutableListOf(la))
						val duplicated = false;
						if (!tmpSet.any { newCi.isSameLr1(it) }) {
							Log.debug("add $newCi")
							tmpSet.add(newCi)
						}
					}
				}
			}
			i++
		}
		section("sort") {
			tmpSet.sort()
			Log.debug("sorted: $tmpSet")
		}

		section("marge") {
			val tmp = tmpSet.toMutableList()
			closureSet.clear()
			var lookaheads = mutableListOf<Token>()
			tmp.forEachIndexed { i, t ->
				lookaheads.add(t.lookaheads[0])
				if (i == tmp.lastIndex || !t.isSameLr0(tmp[i + 1])) {
					closureSet.add(t.copy(syntax = syntax, lookaheads = lookaheads).also { Log.debug("$it") })
					lookaheads = mutableListOf()
				}
			}
		}
	}

	fun isSameLr0(that: ClosureSet) = this.lr0Hash == that.lr0Hash

	fun isSameLr1(that: ClosureSet) = this.lr1Hash == that.lr1Hash

	val toString by lazy { "ClosureSet(${closureSet.joinToString(separator="|", prefix="", postfix="")})" }
	override fun toString(): String = toString
}

