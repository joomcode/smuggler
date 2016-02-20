package io.mironov.smuggler.compiler

internal class GenerationEnvironment(
    val registry: ClassRegistry
) {
  fun newClassWriter(): ClassWriter {
    return ClassWriter(this)
  }

  fun newClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return newClassWriter().apply(visitor).toByteArray()
  }
}
