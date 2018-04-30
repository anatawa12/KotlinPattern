package com.anatawa12.parser.logging

import java.io.PrintStream
import java.io.PrintWriter
import java.util.*

/**
 * Created by anatawa12 on 2018/04/14.
 */
object Section {
	private var sections = mutableListOf<String>()

	fun start(name: String) {
		Log.debug("start $name {")
		sections.add(name)
	}

	fun end(){
		sections.removeAt(sections.lastIndex)
		Log.debug("}")
	}

	val highestSection
		get() = sections.last()

	val allSections: List<String> get() = sections

	val sectionCount
		get() = sections.size
}

inline fun <T>section(name: String, block: ()->T): T {
	Section.start(name)
	try {
		return block()
	} catch (throwable: InSectionException) {
		throw throwable
	} catch (throwable: Throwable) {
		throw InSectionException(Section.highestSection, throwable)
	} finally {
		Section.end()
	}
}

class InSectionException(message: String, cause: Throwable) : Exception(message, cause) {
	fun printStackTrace() {
		printStackTrace(System.err)
	}

	fun printStackTrace(s: PrintStream) {
		printStackTrace(WrappedPrintStream(s))
	}

	fun printStackTrace(s: PrintWriter) {
		printStackTrace(WrappedPrintWriter(s))
	}
	private val CAUSE_CAPTION = "Caused by: "

	private val SUPPRESSED_CAPTION = "Suppressed: "

	private fun printStackTrace(s: PrintStreamOrWriter) {
		cause!!
		// Guard against malicious overrides of Throwable.equals by
		// using a Set with identity equality semantics.
		val dejaVu = Collections.newSetFromMap<Throwable>(IdentityHashMap<Throwable, Boolean>())
		dejaVu.add(this)

		synchronized(s.lock()) {
			// Print our stack trace
			s.println(cause)
			Section.allSections.forEach {
				s.println("\tin $it")
			}
			val trace = cause.stackTrace
			for (traceElement in trace)
				s.println("\tat $traceElement")

			// Print suppressed exceptions, if any
			for (se in cause.suppressed)
				se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu)

			// Print cause, if any
			val ourCause = cause.cause
			ourCause?.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu)
		}
	}

	private fun Throwable.printEnclosedStackTrace(s: PrintStreamOrWriter,
										enclosingTrace: Array<StackTraceElement>,
										caption: String,
										prefix: String,
										dejaVu: MutableSet<Throwable>) {
		assert(Thread.holdsLock(s.lock()))
		if (dejaVu.contains(this)) {
			s.println("\t[CIRCULAR REFERENCE:" + this + "]")
		} else {
			dejaVu.add(this)
			// Compute number of frames in common between this and enclosing trace
			val trace = getStackTrace()
			var m = trace.size - 1
			var n = enclosingTrace.size - 1
			while (m >= 0 && n >= 0 && trace[m] == enclosingTrace[n]) {
				m--
				n--
			}
			val framesInCommon = trace.size - 1 - m

			// Print our stack trace
			s.println(prefix + caption + this)
			for (i in 0..m)
				s.println(prefix + "\tat " + trace[i])
			if (framesInCommon != 0)
				s.println("$prefix\t... $framesInCommon more")

			// Print suppressed exceptions, if any
			for (se in suppressed)
				se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION,
						prefix.toString() + "\t", dejaVu)

			// Print cause, if any
			val ourCause = cause
			ourCause?.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, prefix, dejaVu)
		}
	}


	/**
	 * Wrapper class for PrintStream and PrintWriter to enable a single
	 * implementation of printStackTrace.
	 */
	private abstract class PrintStreamOrWriter {
		/** Returns the object to be locked when using this StreamOrWriter  */
		internal abstract fun lock(): Any

		/** Prints the specified string as a line on this StreamOrWriter  */
		internal abstract fun println(o: Any)
	}

	private class WrappedPrintStream internal constructor(private val printStream: PrintStream) : PrintStreamOrWriter() {

		override fun lock(): Any {
			return printStream
		}

		override fun println(o: Any) {
			printStream.println(o)
		}
	}

	private class WrappedPrintWriter internal constructor(private val printWriter: PrintWriter) : PrintStreamOrWriter() {

		override fun lock(): Any {
			return printWriter
		}

		override fun println(o: Any) {
			printWriter.println(o)
		}
	}
}
