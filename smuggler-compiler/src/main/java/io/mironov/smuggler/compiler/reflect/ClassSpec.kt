package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.annotations.AnnotationProxy
import io.mironov.smuggler.compiler.common.Opener
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.Arrays

internal data class ClassSpec(
    val access: Int,
    val type: Type,
    val parent: Type,
    val signature: String?,
    val interfaces: Collection<Type>,
    val annotations: Collection<AnnotationSpec>,
    val fields: Collection<FieldSpec>,
    val methods: Collection<MethodSpec>,
    val opener: Opener
) {
  internal class Builder(val access: Int, val type: Type, val parent: Type, val opener: Opener) {
    private val interfaces = ArrayList<Type>()
    private val annotations = ArrayList<AnnotationSpec>()
    private val fields = ArrayList<FieldSpec>()
    private val methods = ArrayList<MethodSpec>()
    private var signature: String? = null

    fun signature(signature: String?): ClassSpec.Builder = apply {
      this.signature = signature
    }

    fun interfaces(values: Collection<Type>): ClassSpec.Builder = apply {
      interfaces.addAll(values)
    }

    fun annotation(annotation: AnnotationSpec): ClassSpec.Builder = apply {
      annotations.add(annotation)
    }

    fun field(field: FieldSpec): ClassSpec.Builder = apply {
      fields.add(field)
    }

    fun method(method: MethodSpec): ClassSpec.Builder = apply {
      methods.add(method)
    }

    fun build(): ClassSpec {
      return ClassSpec(access, type, parent, signature, interfaces, annotations, fields, methods, opener)
    }
  }

  fun getConstructor(vararg args: Type): MethodSpec? {
    return getDeclaredMethod("<init>", *args)
  }

  fun getDeclaredMethod(name: String, descriptor: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && it.type.descriptor == descriptor
    }
  }

  fun getDeclaredMethod(name: String, vararg args: Type): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && Arrays.equals(it.arguments, args)
    }
  }

  fun getDeclaredField(name: String): FieldSpec? {
    return fields.firstOrNull {
      it.name == name
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
