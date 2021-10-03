package com.joom.smuggler.compiler

import com.joom.grip.Grip

internal class GenerationEnvironment(val grip: Grip) {
  fun newClassWriter(): ClassWriter {
    return ClassWriter(this)
  }

  fun newClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return newClassWriter().apply(visitor).toByteArray()
  }
}
