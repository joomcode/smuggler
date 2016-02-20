package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.kind
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.reflect.AnnotationSpec
import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.FieldSpec
import kotlin.reflect.jvm.internal.impl.serialization.ClassData
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal class OptionalAware(private val spec: ClassSpec) {
  private companion object {
    private val IS_ANNOTATION_NULLABLE: (spec: AnnotationSpec) -> Boolean = {
      it.type.className.endsWith(".Nullable")
    }

    private val IS_ANNOTATION_NOT_NULL: (spec: AnnotationSpec) -> Boolean = {
      it.type.className.endsWith(".NotNull")
    }
  }

  private val metadata by lazy(LazyThreadSafetyMode.NONE) {
    createKotlinMetaData()
  }

  fun isOptional(field: FieldSpec): Boolean {
    if (field.annotations.any(OptionalAware.Companion.IS_ANNOTATION_NOT_NULL)) {
      return false
    }

    if (field.annotations.any(OptionalAware.Companion.IS_ANNOTATION_NULLABLE)) {
      return true
    }

    val resolver = metadata?.nameResolver ?: return false
    val proto = metadata?.classProto ?: return false

    return proto.propertyList.any {
      resolver.getName(it.name).asString() == field.name && it.returnType.nullable
    }
  }

  private fun createKotlinMetaData(): ClassData?  {
    val annotation = spec.getAnnotation<Metadata>() ?: return null

    if (annotation.kind != Metadata.KIND_CLASS) {
      return null
    }

    val strings = annotation.strings
    val data = annotation.data

    return JvmProtoBufUtil.readClassDataFrom(data, strings)
  }
}
