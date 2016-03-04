package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.common.isStatic
import io.mironov.smuggler.compiler.reflect.ClassReference
import io.mironov.smuggler.compiler.signature.GenericType
import io.mironov.smuggler.compiler.signature.MethodSignatureMirror
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal object AutoParcelableClassSpecFactory {
  fun from(reference: ClassReference, registry: ClassRegistry): AutoParcelableClassSpec {
    val spec = registry.resolve(reference, false)
    val metadata = spec.getAnnotation<Metadata>() ?: run {
      throw InvalidAutoParcelableException(reference.type, "Only kotlin classes can implement AutoParcelable interface.")
    }

    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
    val clazz = proto.classProto
    val resolver = proto.nameResolver

    if (!Flags.IS_DATA.get(clazz.flags)) {
      throw InvalidAutoParcelableException(reference.type, "Only data classes can implement AutoParcelable interface.")
    }

    if (!spec.signature.isNullOrBlank()) {
      throw InvalidAutoParcelableException(spec.type, "Generic classes are not supported at the moment.")
    }

    val creator = spec.getDeclaredField("CREATOR")
    val constructor = clazz.constructorList.first {
      !Flags.IS_SECONDARY.get(it.flags)
    }

    if (creator != null && creator.isStatic) {
      throw InvalidAutoParcelableException(spec.type, "AutoParcelable classes shouldn''t declare CREATOR field.")
    }

    val properties = constructor.valueParameterList.mapIndexed { index, parameter ->
      val name = resolver.getName(parameter.name).identifier
      val getter = spec.getDeclaredMethod("component${index + 1}")!!
      val signature = getter.signature

      val generic = if (signature != null) MethodSignatureMirror.read(signature) else null
      val type = getter.returns

      AutoParcelablePropertySpec(name, generic?.returnType ?: GenericType.RawType(type), getter)
    }

    return AutoParcelableClassSpec(spec, properties)
  }
}
