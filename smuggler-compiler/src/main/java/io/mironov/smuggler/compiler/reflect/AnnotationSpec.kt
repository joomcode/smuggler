package io.mironov.smuggler.compiler.reflect

import org.objectweb.asm.Type
import java.util.LinkedHashMap

internal data class AnnotationSpec(
    val type: Type,
    val values: Map<String, Any?>
) {
  internal class Builder(val type: Type) {
    private val values = LinkedHashMap<String, Any?>()

    fun value(name: String, value: Any?): AnnotationSpec.Builder = apply {
      values.put(name, value)
    }

    fun build(): AnnotationSpec {
      return AnnotationSpec(type, values)
    }
  }

  inline fun <reified V : Any> value(name: String): V {
    return values[name] as V
  }
}
