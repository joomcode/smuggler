package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.reflect.ClassReference
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal object DataClassSpecFactory {
  fun from(clazz: ClassReference, registry: ClassRegistry): DataClassSpec? {
    val spec = registry.resolve(clazz, false)
    val metadata = spec.getAnnotation<Metadata>() ?: return null
    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)

    if (!Flags.IS_DATA.get(proto.classProto.flags)) {
      return null
    }

    return DataClassSpec(spec)
  }
}
