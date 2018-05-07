package com.anatawa12.parser.plugin

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.JavaPluginConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.io.File
import javax.inject.Inject

/**
 * Created by anatawa12 on 2018/04/27.
 */
class PluginCore @Inject constructor(private val fileResolver: FileResolver) : Plugin<Project> {
	override fun apply(project: Project) {
		project.run {
			project.plugins.apply(KotlinPluginWrapper::class.java)

			val kotlinSourceSetProvider = KotlinPattenSourceSetProviderImpl(fileResolver)

			val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
			javaPluginConvention.sourceSets.all { sourceSet ->
				tasks.create(sourceSet.getCompileTaskName("kotlinPattern"), CompileKotlinPattern::class.java) { it.run {
					val sourceSetName = sourceSet.name

					val sourceRootDir = "src/$sourceSetName/kpt"

					val kotlinPattenSourceSet = kotlinSourceSetProvider.create(sourceSet.name)
					kotlinPattenSourceSet.kotlinPatten.srcDir(project.file(sourceRootDir))
					sourceSet.addConvention("kotlinPattern", kotlinPattenSourceSet)

					outputDir = File("src/$sourceSetName/kotlin")

					this.kotlinPatternSourceSet = kotlinPattenSourceSet

					source(kotlinPattenSourceSet.kotlinPatten)
					outputs.dir(outputDir)

					val compileKotlinName = sourceSet.getCompileTaskName("kotlin")

					project.tasks.firstOrNull { it.name == compileKotlinName }?.dependsOn?.add(this)
				}}
			}
		}
	}

}

const val KOTLIN_PATTEN_BUILD_DIR_NAME = "kpt"

fun <T, A, R>T.Closure(block: (A) -> R) = object : Closure<R>(this, this) { fun doCall(it: A) = block(it) }


internal inline fun <reified T : Any> Any.addConvention(name: String, plugin: T) {
	(this as HasConvention).convention.plugins[name] = plugin
}

internal inline fun <reified T : Any> Any.getConvention(name: String): T {
	return (this as HasConvention).convention.plugins[name] as T
}
