package com.anatawa12.parser.plugin

import com.anatawa12.parser.frontend.KotlinPatternArgments
import com.anatawa12.parser.frontend.OutputType
import com.anatawa12.parser.frontend.kotlinPatten
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File
import java.util.*


/**
 * Created by anatawa12 on 2018/04/27.
 */
@CacheableTask
open class CompileKotlinPattern : SourceTask() {
	lateinit var kotlinPatternSourceSet: KotlinPatternSourceSet
	lateinit var outputDir: File

	@TaskAction
	fun execute(inputs: IncrementalTaskInputs) {
		kotlinPatternSourceSet.kotlinPatten.files
				.filter { file -> file.nameWithoutExtension == "parser" }
				.forEachIndexed { index, inFile ->
					val outFile = outputDir
					kotlinPatten(KotlinPatternArgments(inFile, outFile, OutputType.PackageRootDir))
				}
	}
}

internal fun ChangedFiles(taskInputs: IncrementalTaskInputs): ChangedFiles {
	if (!taskInputs.isIncremental) return ChangedFiles.Unknown()

	val modified = ArrayList<File>()
	val removed = ArrayList<File>()

	taskInputs.outOfDate { modified.add(it.file) }
	taskInputs.removed { removed.add(it.file) }

	return ChangedFiles.Known(modified, removed)
}

sealed class ChangedFiles private constructor() {
	data class Known constructor(val modified: kotlin.collections.List<java.io.File>, val removed: kotlin.collections.List<java.io.File>) : ChangedFiles()

	class Unknown : ChangedFiles () {
		override fun toString(): String {
			return "Unknown()"
		}
	}
}