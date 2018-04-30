package com.anatawa12.parser.plugin

import groovy.lang.Closure
import org.gradle.api.file.SourceDirectorySet

/**
 * Created by anatawa12 on 2018/04/29.
 */
interface KotlinPatternSourceSet {
	val kotlinPatten: SourceDirectorySet

	fun kotlinPatten(configureClosure: Closure<Any?>?): KotlinPatternSourceSet
}