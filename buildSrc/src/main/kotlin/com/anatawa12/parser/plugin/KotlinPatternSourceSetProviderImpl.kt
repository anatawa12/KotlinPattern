package com.anatawa12.parser.plugin

import groovy.lang.Closure
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.util.ConfigureUtil
import java.lang.reflect.Constructor

/**
 * Created by anatawa12 on 2018/04/29.
 */
internal class KotlinPattenSourceSetProviderImpl constructor(private val fileResolver: FileResolver) : KotlinPattenSourceSetProvider {
	override fun create(displayName: String): KotlinPatternSourceSet =
			KotlinPattenSourceSetImpl(displayName, fileResolver)
}

private class KotlinPattenSourceSetImpl(displayName: String, resolver: FileResolver) : KotlinPatternSourceSet {
	override val kotlinPatten: SourceDirectorySet =
			createDefaultSourceDirectorySet("$displayName Kotlin patten source", resolver)

	init {
		kotlinPatten.filter.include("**/*.ktp")
	}

	override fun kotlinPatten(configureClosure: Closure<Any?>?): KotlinPatternSourceSet {
		ConfigureUtil.configure(configureClosure, kotlinPatten)
		return this
	}
}

private val createDefaultSourceDirectorySet: (name: String?, resolver: FileResolver?) -> SourceDirectorySet = run {
	val klass = DefaultSourceDirectorySet::class.java
	val defaultConstructor = klass.constructorOrNull(String::class.java, FileResolver::class.java)

	if (defaultConstructor != null && defaultConstructor.getAnnotation(java.lang.Deprecated::class.java) == null) {
		// TODO: drop when gradle < 2.12 are obsolete
		{ name, resolver -> defaultConstructor.newInstance(name, resolver) }
	}
	else {
		val directoryFileTreeFactoryClass = Class.forName("org.gradle.api.internal.file.collections.DirectoryFileTreeFactory")
		val alternativeConstructor = klass.getConstructor(String::class.java, FileResolver::class.java, directoryFileTreeFactoryClass)

		val defaultFileTreeFactoryClass = Class.forName("org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory")
		val defaultFileTreeFactory = defaultFileTreeFactoryClass.getConstructor().newInstance()
		return@run { name, resolver -> alternativeConstructor.newInstance(name, resolver, defaultFileTreeFactory) }
	}
}

private fun <T> Class<T>.constructorOrNull(vararg parameterTypes: Class<*>): Constructor<T>? =
		try {
			getConstructor(*parameterTypes)
		}
		catch (e: NoSuchMethodException) {
			null
		}
