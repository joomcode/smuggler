package io.mironov.smuggler.compiler.annotations

@AnnotationDelegate("kotlin.Metadata")
interface Metadata {
  companion object {
    const val KIND_CLASS = 1
    const val KIND_FILE = 2
    const val KIND_SYNTHETIC_CLASS = 3
    const val KIND_MULTIFILE_CLASS_FACADE = 4
    const val KIND_MULTIFILE_CLASS_PART = 5
  }

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
