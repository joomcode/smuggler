package com.joom.smuggler.compiler

import com.joom.smuggler.compiler.common.Types
import org.objectweb.asm.Type
import java.util.NoSuchElementException

internal data class GeneratedContent(
  val path: String,
  val extras: Map<String, Any>,
  val content: ByteArray
) {
  companion object {
    fun from(type: Type, extras: Map<String, Any>, content: ByteArray): GeneratedContent {
      return GeneratedContent(Types.getClassFilePath(type), extras, content)
    }
  }

  inline fun <reified T> extra(name: String): T {
    return extras[name] as T ?: throw NoSuchElementException(name)
  }

  fun has(name: String): Boolean {
    return extras.containsKey(name)
  }
}
