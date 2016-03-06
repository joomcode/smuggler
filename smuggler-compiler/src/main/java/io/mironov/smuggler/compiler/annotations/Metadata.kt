package io.mironov.smuggler.compiler.annotations

@AnnotationDelegate("kotlin.Metadata")
interface Metadata {
  fun k(): Int

  fun d1(): Array<String>

  fun d2(): Array<String>
}

val Metadata.data: Array<String>
  get() = d1()

val Metadata.strings: Array<String>
  get() = d2()

val Metadata.kind: Int
  get() = k()
