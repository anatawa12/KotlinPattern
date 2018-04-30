package com.anatawa12.parser.logging

/**
 * Created by anatawa12 on 2018/04/14.
 */
object Log {
	fun debug(string: String) {
		/*
		repeat(Section.sectionCount) {
			print("  ")
		}
		println(string)
		// */
	}
	fun debug(string: Any?) = debug(string.toString())
}