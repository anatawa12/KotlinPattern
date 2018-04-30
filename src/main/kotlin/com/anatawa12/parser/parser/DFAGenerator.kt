package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.InitMutableMap
import com.anatawa12.parser.logging.Log
import com.anatawa12.parser.logging.Section
import com.anatawa12.parser.logging.section

/**
 * Created by anatawa12 on 2018/04/15.
 */
typealias DFAEdge = MutableMap<Token, Int>
typealias DFA = MutableList<DFANode>
data class DFANode (val closure: ClosureSet, val edge: DFAEdge)

class DFAGenerator(val syntax: SyntaxDB) {
	val LR1DFA: DFA
	//val LALR1DFA: RegDFA

	init {
		Section.start("Generate DFA")
		val initItem = ClosureItem(syntax, -1, 0, mutableListOf(Token.Eof))
		val initSet = ClosureSet(syntax, mutableListOf(initItem))

		val dfa = mutableListOf(DFANode(initSet, mutableMapOf()))
		do {
			var changed = false
			var i = 0
			while (i < dfa.size) {
				val closure = dfa[i].closure
				val edge = dfa[i].edge
				val newSets = generateNewClosureSets(closure)
				for ((edgeLabel, cs) in newSets) {
					val newNode = DFANode(cs, mutableMapOf())
					val duplictedIndex = indexOfDuplicatedNode(dfa, newNode)
					val indexTo: Int
					if (duplictedIndex == -1) {
						dfa.add(newNode)
						indexTo = dfa.lastIndex
						changed = true
					} else {
						indexTo = duplictedIndex
					}

					if (edgeLabel !in edge) {
						edge[edgeLabel] = indexTo
						changed = true
						dfa[i] = DFANode(closure, edge)
					}
				}
				i++
			}
		} while (changed)
		LR1DFA = dfa
		//lalrDfa = mergeLA(dfa)
		Section.end()
	}

	private fun generateNewClosureSets(closureSet: ClosureSet): Map<Token, ClosureSet> = section("NewClosureSets for $closureSet"){
		val tmp = InitMutableMap<Token, MutableList<ClosureItem>>(::mutableListOf)
		for ((_, syntaxId, dotIndex, lookaheads) in closureSet.closureSet) {
			val (ltoken, pattern) = syntax.get(syntaxId)
			if (dotIndex == pattern.size) continue
			val newCi = ClosureItem(syntax, syntaxId, dotIndex + 1, lookaheads)
			val edgeLabel = pattern[dotIndex]

			val items = mutableListOf<ClosureItem>()
			if (edgeLabel in tmp) {
				items.addAll(tmp[edgeLabel])
			}
			items.add(newCi)

			tmp[edgeLabel] = items
		}
		Log.debug("tmp: $tmp")

		return tmp.map { (edgeLabel, items) -> edgeLabel to ClosureSet(syntax, items) }
				.toMap()
	}

	private fun indexOfDuplicatedNode(dfa: DFA, newNode: DFANode)
			= dfa.mapIndexed { i, node -> if(node.closure.isSameLr1(newNode.closure)) i else null }
			.filterNotNull()
			.singleOrNull() ?: -1
}

