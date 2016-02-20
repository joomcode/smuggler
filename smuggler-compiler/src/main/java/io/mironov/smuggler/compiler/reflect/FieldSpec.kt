package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.annotations.AnnotationProxy
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class FieldSpec(
    val access: Int,
    val name: String,
    val type: Type,
    val annotations: Collection<AnnotationSpec> = emptyList()
) {
  internal class Builder(val access: Int, val name: String, val type: Type) {
    private val annotations = ArrayList<AnnotationSpec>()

    fun annotation(annotation: AnnotationSpec): FieldSpec.Builder = apply {
      annotations.add(annotation)
    }

    fun build(): FieldSpec {
      return FieldSpec(access, name, type, annotations)
    }
  }

  inline fun <reified A : Any> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  fun <A> getAnnotation(annotation: Class<A>): A? {
    return AnnotationProxy.create(annotation, annotations.firstOrNull {
      it.type == Types.getAnnotationType(annotation)
    } ?: return null)
  }
}
