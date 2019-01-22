package com.anatawa12.parser.parser

import com.anatawa12.libs.collections.InitMap
import com.anatawa12.libs.collections.toMapList
import com.anatawa12.libs.coroutines.coroutineScope
import com.anatawa12.parser.KotlinPatternArguments
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import kotlin.properties.Delegates

/**
 * Created by anatawa12 on 2018/04/15.
 */

fun SyntaxDefinitions.printString(indent: String = "") = joinToString(prefix = indent, separator = "$indent\n")

data class ClosureItem(val syntax: SyntaxDefinitionSection, val dotIndex: Int, val lookaheads: Set<Token>) {
	private val _toString: String by lazy {
		val builder = StringBuilder("ClosureItem(")
		builder.append(syntax.ltoken.name.toWritable())
		builder.append(" -> (")
		syntax.pattern.forEachIndexed { i, it->
			if (i == dotIndex)
				if (i == 0) builder.append(" . ")
				else builder.append(". ")
			builder.append(it.name.toWritable())
			if (i != syntax.pattern.lastIndex) builder.append(' ')
		}
		if (dotIndex == syntax.pattern.size) builder.append(" . ")
		builder.append("))")
		builder.toString()
	}
	override fun toString() = _toString
}
typealias SyntaxDB = Map<Token, SyntaxDefinitions>


class ClosureSet(val set: Set<ClosureItem>) {
	private val _toString: String by lazy { toString("") }
	override fun toString() = _toString

	fun toString(indent: String): String {
		val builder = StringBuilder(indent)
		builder.append("ClosureSet(\n")
		set.forEach {
			builder.append(indent)
			builder.append("    ")
			builder.append(it)
			builder.append('\n')
		}
		builder.append(indent)
		builder.append(')')
		return builder.toString()
	}

	fun equalsWithLR0(other: ClosureSet): Boolean {
		if (set.size != other.set.size) return false
		for (closureItem in this.set) {
			if(!other.set.any { closureItem.syntax == it.syntax && closureItem.dotIndex == it.dotIndex }) {
				return false
			}
		}
		return true
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ClosureSet

		if (set != other.set) return false

		return true
	}

	override fun hashCode(): Int {
		return set.hashCode()
	}
}

data class FirstSet(private val map: Map<Token, Set<Token>>, private val nullSet: Set<Token>) {
	operator fun get(token: Token) = map[token] ?: setOf(token)

	operator fun get(list: List<Token>, firstIndex: Int, lookaheds: Set<Token>): Set<Token> {
		if (list.size == firstIndex) return lookaheds
		val result = mutableSetOf<Token>()
		var index = firstIndex

		while (index < list.size) {
			val token = list[index]
			result.addAll(this[token])
			if (token in nullSet) return result
			index ++
		}
		result.addAll(lookaheds)
		return result
	}
}

class DFANode(val closureSet: ClosureSet, val nextNodes: Map<Token, DFANode>) {
	var index: Int by Delegates.notNull()

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as DFANode

		if (closureSet != other.closureSet) return false

		return true
	}

	override fun hashCode(): Int {
		return closureSet.hashCode()
	}
}

fun generateNullSet(syntaxDefinitions: SyntaxDefinitions): Set<Token> {
	val nullSet = mutableSetOf<Token>()
	do {
		var isChanged = false
		for (syntaxDefinition in syntaxDefinitions) {
			if (syntaxDefinition.pattern.isEmpty() || syntaxDefinition.pattern.all { it in nullSet })
				if (nullSet.add(syntaxDefinition.ltoken))
					isChanged = true
		}
	} while (isChanged)
	return nullSet
}

fun generateFirstMap(syntaxDefinitions: SyntaxDefinitions, nullSet: Set<Token>, tokenSet: Set<Token>): FirstSet {
	val map = InitMap<Token, MutableSet<Token>> { mutableSetOf() }
	tokenSet.asSequence()
			.filter { it.type == TokenType.Terminal }
			.forEach {
				map[it].add(it)
			}
	do {
		var isChanged = false
		for (syntaxDefinition in syntaxDefinitions) {
			for (token in syntaxDefinition.pattern) {
				if (map[syntaxDefinition.ltoken].addAll(map[token])) isChanged = true
				if (token !in nullSet) break
			}
		}
	} while (isChanged)
	return FirstSet(map.map, nullSet)
}

fun setEveryTokenAndDefinitionId(defections: SyntaxDefinitions): Set<Token> {
	var nextTokenId = 0
	val nonTerminals = mutableSetOf<Token>()
	val tokenMap = InitMap<Token, Int> { nextTokenId++ }
	defections.forEach {
		nonTerminals.add(it.ltoken)
	}
	defections.forEach { defection ->
		defection.pattern.forEach patterns@{
			if (it in nonTerminals) return@patterns
			val id = tokenMap[it]
			it.resultId = id
			it.runTimeId = id + 1
		}
	}
	val id = tokenMap[Token.Eof]
	Token.Eof.resultId = id
	Token.Eof.runTimeId = id + 1
	var nextDfinitionId = 0
	defections.forEach {
		it.id = nextDfinitionId++
		val id = tokenMap[it.ltoken]
		it.ltoken.resultId = id
		it.ltoken.runTimeId = id + 1
	}
	return tokenMap.keys
}

enum class TokenType {
	/**
	 * 終端記号
	 */
	Terminal,
	/**
	 * 非終端記号
	 */
	NonTerminal
}

fun String.toWritable(): String = kotlin.run {
	val builder = StringBuilder()
	for (c in this) {
		when (c) {
			'\t' -> builder.append("\\t")
			'\b' -> builder.append("\\b")
			'\n' -> builder.append("\\n")
			'\r' -> builder.append("\\r")
			'\\' -> builder.append("\\\\")
			else -> builder.append(c)
		}
	}
	return builder.toString()
}

fun generateSyntaxDB(syntaxDefinitions: SyntaxDefinitions): SyntaxDB {
	val syntaxDB = InitMap<Token, MutableList<SyntaxDefinitionSection>>() { mutableListOf() }
	for (syntaxDefinition in syntaxDefinitions) {
		syntaxDB[syntaxDefinition.ltoken].add(syntaxDefinition)
	}
	return syntaxDB
}

fun expendClosureItemSet(inSet: Set<ClosureItem>, syntaxDB: SyntaxDB, firstSet: FirstSet): ClosureSet {
	val set = mutableSetOf<ClosureItem>()
	val list = mutableListOf<ClosureItem>()

	inSet.forEach { inItem ->
		inItem.lookaheads.forEach {
			val item = inItem.copy(lookaheads = setOf(it))
			set.add(item)
			list.add(item)
		}
	}

	var i = 0
	while (list.size > i) {
		run {
			val inputItem = list[i]
			if (inputItem.syntax.pattern.size == inputItem.dotIndex) return@run
			var readIndex = inputItem.dotIndex
			val curToken = inputItem.syntax.pattern[readIndex]
			if (curToken.type != TokenType.NonTerminal) return@run
			val syntaxes = syntaxDB[curToken]!!

			val lockaheds = firstSet[inputItem.syntax.pattern, inputItem.dotIndex + 1, inputItem.lookaheads]

			syntaxes.forEach { section ->
				lockaheds.forEach lockaheds@{
					val item = ClosureItem(section, 0, setOf(it))
					if (item in set) {
						return@lockaheds
					}
					set.add(item)
					list.add(item)
				}
			}
		}
		i++
	}

	return ClosureSet(list.map { (it.syntax to it.dotIndex) to it }
			.toMapList()
			.map { (_, list) ->
				list.first().copy(lookaheads = list.map { it.lookaheads.single() }.toSet())
			}.toSet())
}

fun createNextClosureSet(inSet: ClosureSet): Map<Token, Set<ClosureItem>> {
	val nextMap = InitMap<Token, MutableSet<ClosureItem>> { mutableSetOf() }
	for (closureItem in inSet.set) {
		if (closureItem.dotIndex == closureItem.syntax.pattern.size) continue
		nextMap[closureItem.syntax.pattern[closureItem.dotIndex]]
				.add(closureItem.copy(dotIndex = closureItem.dotIndex + 1))
	}
	return nextMap.mapValues { (_, nextItems) -> nextItems.toSet() }
}

@Throws(ReduceReduceConflictException::class)
suspend fun generates(grammarDefinition: GrammarDefinition, args: KotlinPatternArguments): ParsingResult {
	val initSyntaxDefinitionSection = SyntaxDefinitionSection(Token.Syntax, listOf(grammarDefinition.start_symbol))
	val initClosureItem = ClosureItem(initSyntaxDefinitionSection, 0, setOf(Token.Eof))

	val syntaxDefinitions = mutableListOf<SyntaxDefinitionSection>()
			.also { it.add(initSyntaxDefinitionSection) }
			.also { it.addAll(grammarDefinition.syntax) }

	val tokenSet = setEveryTokenAndDefinitionId(syntaxDefinitions)
	val nullSet = generateNullSet(syntaxDefinitions)
	val firstSet= generateFirstMap(syntaxDefinitions, nullSet, tokenSet)
	val syntaxDB = generateSyntaxDB(syntaxDefinitions)

	val dfaNodeList = mutableListOf<DFANode>()
	val dfaIndexMap = mutableMapOf<DFANode, Int>()

	val mutex = Mutex()

	coroutineScope().launch { generateDfaNode({}, setOf(initClosureItem), dfaNodeList, dfaIndexMap, syntaxDB, firstSet, mutex) }.join()
	println()
	println("DFA node count: ${dfaNodeList.size}")
	return ParsingResult(generateParsingTable(dfaNodeList, args), tokenSet, syntaxDefinitions)
}

var printSharpCount = 0

fun printSharp() {
	print("#")
	printSharpCount++
	if (printSharpCount == 100) {
		printSharpCount = 0
		println()
	}
}

suspend fun generateDfaNode(setter: (DFANode) -> Unit, itemSet: Set<ClosureItem>, dfaNodeList: MutableList<DFANode>, dfaIndexMap: MutableMap<DFANode, Int>, syntaxDB: SyntaxDB, firstSet: FirstSet, mutex: Mutex) {
	val closureSet = expendClosureItemSet(itemSet, syntaxDB, firstSet)
	val nextNode = mutableMapOf<Token, DFANode>()
	val dfaNode = DFANode(closureSet, nextNode)
	var isCreate = false
	val index = mutex.withLock<Int> {
		var index = dfaIndexMap[dfaNode]
		if (index == null) {
			dfaNodeList.add(dfaNode)
			index = dfaNodeList.lastIndex
			dfaIndexMap[dfaNode] = index
			isCreate = true
			dfaNode.index = index
			printSharp()
		}
		return@withLock index
	}
	setter(dfaNodeList[index])
	if (isCreate) {
		val map = createNextClosureSet(closureSet)
		for ((token, set) in map) {
			coroutineScope().launch(coroutineContext) { generateDfaNode({ nextNode[token] = it }, set, dfaNodeList, dfaIndexMap, syntaxDB, firstSet, mutex) }
		}
	}
}

@Throws(ReduceReduceConflictException::class)
fun generateParsingTable(dfa: List<DFANode>, args: KotlinPatternArguments): ParsingTable {
	val parsingTable = Array<Map<Token, Operation>>(dfa.size) { mapOf() }

	dfa.forEachIndexed { index, dfaNode ->
		val map = mutableMapOf<Token, Operation>()
		for ((token, nextnode) in dfaNode.nextNodes) {
			map[token] = when (token.type) {
				TokenType.Terminal -> {
					Operation.Shift(nextnode.index, nextnode.closureSet.set.toList())
				}
				TokenType.NonTerminal -> {
					Operation.Goto(nextnode.index, nextnode.closureSet.set.toList())
				}
			}
		}
		for (closureItem in dfaNode.closureSet.set) {
			if (closureItem.dotIndex == closureItem.syntax.pattern.size) {
				if (closureItem.syntax.ltoken == Token.Syntax) {
					map[Token.Eof] = Operation.Accept
				} else {
					for (lookahed in closureItem.lookaheads) {
						val old = map[lookahed]
						val newInsn = Operation.Reduce(closureItem.syntax.id, listOf(closureItem))
						when (old) {
							is Operation.Shift -> {
								map[lookahed] = Operation.Conflicted(mutableListOf(old), mutableListOf(newInsn))
							}
							is Operation.Reduce -> {
								map[lookahed] = Operation.Conflicted(mutableListOf(), mutableListOf(newInsn, old))
							}
							is Operation.Goto -> error("don't do here")
							is Operation.Conflicted -> {
								old.reduceSyntax.add(newInsn)
							}
							Operation.Accept -> error("don't do here")
							null -> map[lookahed] = newInsn
						}
					}
				}
			}
		}
		parsingTable[index] = map.mapValues { removeConflict(it, args.input.path) }
	}
	printConflicts(args)
	return parsingTable
}

fun printConflicts(args: KotlinPatternArguments) {
	for ((items, conflictInfo) in conflictes) {
		val (lockaheds, shiftCount, reduceCount) = conflictInfo
		val isError = reduceCount >= 2
		if (isError) continue
		print("[warning]: ")
		repeat(shiftCount) {
			if (it != 0) print('/')
			print("shift")
		}
		if (shiftCount != 0 && reduceCount != 0) print('/')
		repeat(reduceCount) {
			if (it != 0) print('/')
			print("reduce")
		}
		println(" conflict.")
		print("\tin ")
		println(args.input.path)
		items.forEach {
			print("\tat ")
			print(it.dataString())
			print(" (")
			print(it.syntax.fromFileName ?: "<unknown file>")
			print(')')
			println()
		}
		if (args.seeLookaheadsOnError) {
			print("\tfor lookaheads: ")
			println(lockaheds.joinToString { it.name.toWritable() })
		}
	}
	var isErrored = false
	for ((items, conflictInfo) in conflictes) System.err.run {
		val (lockaheds, shiftCount, reduceCount) = conflictInfo
		val isError = reduceCount >= 2
		if (!isError) return@run
		isErrored = true
		print("[error]: ")
		repeat(shiftCount) {
			if (it != 0) print('/')
			print("shift")
		}
		if (shiftCount != 0 && reduceCount != 0) print('/')
		repeat(reduceCount) {
			if (it != 0) print('/')
			print("reduce")
		}
		println(" conflict.")
		print("\tin ")
		println(args.input.path)
		items.forEach {
			print("\tat ")
			print(it.dataString())
			print(" (")
			print(it.syntax.fromFileName ?: "<unknown file>")
			print(')')
			println()
		}
		if (args.seeLookaheadsOnError) {
			print("\tfor lookaheads: ")
			println(lockaheds.joinToString { it.name.toWritable() })
		}
	}
	if (isErrored) throw ReduceReduceConflictException()
}

data class ConflictItem(val syntax: SyntaxDefinitionSection, val dotIndex: Int) {
	fun dataString(builder: Appendable = StringBuilder()): String {
		builder.append(syntax.ltoken.name.toWritable())
		builder.append(" -> (")
		syntax.pattern.forEachIndexed { i, it->
			if (i == dotIndex)
				if (i == 0) builder.append(" . ")
				else builder.append(". ")
			builder.append(it.name.toWritable())
			if (i != syntax.pattern.lastIndex) builder.append(' ')
		}
		if (dotIndex == syntax.pattern.size) builder.append(" . ")
		builder.append(")")
		return builder.toString()
	}

	companion object {
		fun of(closureItem: ClosureItem) = ConflictItem(closureItem.syntax, closureItem.dotIndex)
	}
}

data class ConflictInfo(val lookaheads: MutableSet<Token>, val shiftCount: Int, val reduceCount: Int)

val conflictes = mutableMapOf<Set<ConflictItem>, ConflictInfo>()

fun removeConflict(entry: Map.Entry<Token, Operation>, filePath: String): Operation = entry.let { (key, operation) -> when(operation) {
	is Operation.Shift, is Operation.Goto, is Operation.Reduce, Operation.Accept -> operation
	is Operation.Conflicted -> {
		val itemMap = operation.syntaxes.map { ConflictItem.of(it) to it }.toMap()

		conflictes.getOrPut(itemMap.keys) { ConflictInfo(mutableSetOf(), operation.shiftTo.size, operation.reduceSyntax.size) }.lookaheads.add(key)

		(operation.shiftTo.firstOrNull() ?: operation.reduceSyntax.first())
	}}
}
