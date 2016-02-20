package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.annotations.AnnotationProxy
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class MethodSpec(
    val access: Int,
    val name: String,
    val type: Type,
    val signature: String? = null,
    val annotations: Collection<AnnotationSpec> = emptyList()
) {
  internal class Builder(val access: Int, val name: String, val type: Type, val signature: String?) {
    private val annotations = ArrayList<AnnotationSpec>()

    fun annotation(annotation: AnnotationSpec): MethodSpec.Builder = apply {
      annotations.add(annotation)
    }

    fun build(): MethodSpec {
      return MethodSpec(access, name, type, signature, annotations)
    }
  }

  val returns by lazy(LazyThreadSafetyMode.NONE) {
    type.returnType
  }

  val arguments by lazy(LazyThreadSafetyMode.NONE) {
    type.argumentTypes.orEmpty()
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
