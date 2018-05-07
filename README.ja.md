#KotlinPattern

* [コレは何？](#コレは何？)
* [使い方](#使い方)
  * [文](文)
  * [ファイル生成](#ファイル生成)
* [完全な文法定義](#完全な文法定義)

## コレは何？
This is a LR parser generator for kotlin.
This generator's result is kotlin file.

## 使い方
### 文
`@package <package>`<br/>
これは生成されファイルのパッケージを指定します。
例えば`com.anatawa12.frontend.generated`などです。

`@importFile ( <file-name> )` <br/>
これは`<file-name>`から文法定義を読み込みます。
`<file-name>` は文字列です。
例えば`"./statements.kpt"`や`"./main.kpt"`などです。

`@importFile <package>` <br/>
生成されファイルに`<package>`をインポートさせます
例えば`kotlin.reflect.KProperty`や`kotlin.reflect.*`や`kotlin.reflect.KProperty as Property`などです

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
コレはパターンを追加します。
もし型を省略するのであればこのバターンの型は`Unit`になります。

### ファイル生成
以下のコマンドを実行してください。 <br />
`java -jar build/libs/KotlinPattern-0.0.1.jar [options] [input] [output]`
#### options
##### `-o <file>`, `--output <file>`
`<file>` を出力に指定します。
##### `-i <file>`, `--input <file>`
`<file>` を入力ファイルに指定します。
##### `-p`, `--package-root`
出力をディレクトリとして、入力のパッケージに合わせた位置に `Parser.kt` を出力します。
##### `-f`, `--file-path`
出力のいちにそのまま出力します。デフォルトの動作です。


### ast generatorの作成
`SyntaxAstGenerator`はこのような感じです。
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
このインターフェースを実装してください。

### Syntaxの実行
`SyntaxRunnner`のシグネチャーはコレです。
```kotlin
class SyntaxRunner(_lexer: ()-> Token, generator: SyntaxAstGenerator)
```
まず`SyntaxRunner`を生成して、`SyntaxRunner#run`を実行してください。
`SyntaxRunner`はスレッドセーフではありません。

## 完全な文法定義 
コレは [Kotlinの文法定義](https://kotlinlang.org/docs/reference/grammar.html)を使用してます。
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