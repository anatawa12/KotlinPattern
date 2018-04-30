package com.anatawa12.parser.plugin

/**
 * Created by anatawa12 on 2018/04/29.
 */
internal interface KotlinPattenSourceSetProvider {
	fun create(displayName: String): KotlinPatternSourceSet
}