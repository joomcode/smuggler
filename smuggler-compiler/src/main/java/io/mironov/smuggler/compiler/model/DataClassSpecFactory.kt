package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.reflect.ClassReference
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal object DataClassSpecFactory {
  fun from(reference: ClassReference, registry: ClassRegistry): DataClassSpec? {
    val spec = registry.resolve(reference, false)

    val metadata = spec.getAnnotation<Metadata>() ?: return null
    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)

    val clazz = proto.classProto
    val resolver = proto.nameResolver

    if (!Flags.IS_DATA.get(clazz.flags)) {
      return null
    }

    val constructor = clazz.constructorList.first { !Flags.IS_SECONDARY.get(it.flags) }
    val fields = constructor.valueParameterList.map { resolver.getName(it.name).identifier }

    return DataClassSpec(spec, fields)
  }
}
