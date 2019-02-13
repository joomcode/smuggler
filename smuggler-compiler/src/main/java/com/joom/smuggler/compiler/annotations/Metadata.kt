package com.joom.smuggler.compiler.annotations

@AnnotationDelegate("kotlin.Metadata")
internal interface Metadata {
  fun d1(): Array<String>

  fun d2(): Array<String>
}

internal val Metadata.data: Array<String>
  get() = d1()

internal val Metadata.strings: Array<String>
  get() = d2()
