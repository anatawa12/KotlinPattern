# KotlinPattern

This library will be obsoleted. I'm making new parser generator

* [what is this](#What-is-this)
* [how to use](#How-To-Use)
  * [statements](statements)
  * [generate file](#generate-file)
  * [make ast generator](#make-ast-generator)
  * [run the Syntax](#run-the-Syntax)
* [The Complete Syntax](#The-Complete-Syntax)

## What is this
This is a LR parser generator for kotlin.
This generator's result is kotlin file.

## How To Use
### statements
`@package <package>`<br/>
This is to set package in generated source code.
For example, `com.anatawa12.frontend.generated`

`@importFile ( <file-name> )` <br/>
This is to importFile file form `<file-name>`.
`<file-name>` is a string literal. 
For example, `"./statements.kpt"` and `"./main.kpt"`

`@importFile <package>` <br/>
This is to importFile `<package>` in generated source code.
For example, `kotlin.reflect.KProperty`, `kotlin.reflect.*` and  `kotlin.reflect.KProperty as Property`

```
<patten-name>
  : <pattern1-token1> <pattern1-token2> ...
  : <pattern2-token1> <pattern2-token2> ...
  ...
  ;
```
```
<patten-name>: <type-name>
  : <pattern1-token1> <pattern1-token2> ...
  : <pattern2-token1> <pattern2-token2> ...
  ...
  ;
```
This is to add pattern.
if this is not set type, the pattern's type is `Unit`.

### generate file
run the command <br />
`java -jar build/libs/KotlinPattern-0.0.1.jar [options] [input] [output]`
#### options
##### `-o <file>`, `--output <file>`
set the output file `<file>`
##### `-i <file>`, `--input <file>`
set the input file `<file>`
##### `-p`, `--package-root`
Output `Parser.kt` at the position matching the input package, with output as a directory.
##### `-f`, `--files-path`
Output it as it is on the output. This is the default behavior.

### make ast generator
Syntax ast generator is like that
```kotlin
interface SyntaxAstGenerator {
	fun kptFile_0(`TopLebelObject*_0`: MutableList<TopLevelObject>): Kpt
	fun package_0(`"@package"_0`: Token, `SimpleName&"!d"_1`: MutableList<Token>): Package
	fun TopLebelObject_0(importFile_0: TopLevelObject): TopLevelObject
	fun TopLebelObject_1(type_0: TopLevelObject): TopLevelObject
	fun TopLebelObject_2(pattern_0: TopLevelObject): TopLevelObject
	fun TopLebelObject_3(skip_0: TopLevelObject): TopLevelObject
	fun TopLebelObject_4(importPackage_0: TopLevelObject): TopLevelObject
}
```
Please implement the interface

### run the Syntax
`SyntaxRunnner`'s signature is that
```kotlin
class SyntaxRunner(_lexer: ()-> Token, generator: SyntaxAstGenerator)
```
First, make `SyntaxRunner`.
Second, call `SyntaxRunner#run`.
`SyntaxRunner` is not thread safe.

## The Complete Syntax 
This format using [kotlin's grammar format](https://kotlinlang.org/docs/reference/grammar.html)
```
[start]
kptFile
  : TopLebelObject*
  ;

package
  : "@package" SimpleName{"."}
  ;

TopLebelObject
  : importFile
  : type
  : pattern
  : skip
  : importPackage
  ;

importFile
  : "@importFile" "(" string ")"
  ;

importPackage
  : "@importFile" SimpleName{"."}
  : "@importFile" SimpleName{"."} "." "*"
  : "@importFile" SimpleName{"."} "as" SimpleName
  ;

tokenName
  : SimpleName "<" SimpleName ">"
  ;

skip
  : "@skip" SimpleName{","}
  ;

patType
  : ":" typeKt
  ;

pattern
  : SimpleName patType? SEMI "=" orPatterns ";"
  : SimpleName patType? SEMI patterns ";"
  ;

orPatterns
  : PatternElements{"|"}
  ;

patterns
  : ":" PatternElements{":"}
  ;

PatternElements
  : PatternExp*
  ;

PatternExp
  : PatternElement
  : PatternElement "*"
  : PatternElement "+"
  : PatternElement "?"
  : PatternElement "{" PatternElement "}"
  ;

PatternElement
  : string
  : SimpleName
  ;

typeKt
  : typeReference
  ;

typeReference
  : "(" typeReference ")"
  : userType
  : nullableType
  ;

nullableType
  : typeReference "?"
  ;

userType
  : simpleUserType
  : userType "." simpleUserType
  ;

simpleUserType
  : SimpleName
  : SimpleName "<" projectionType{","} ">"
  ;

projectionType
  : projection? typeKt
  : "*"
  ;

projection
  : varianceAnnotation
  ;

varianceAnnotation
  : "in"
  : "out"
  ;

SEMI
  : LF
  : ";"
  : SEMI LF
  : SEMI ";"
  ;
```
