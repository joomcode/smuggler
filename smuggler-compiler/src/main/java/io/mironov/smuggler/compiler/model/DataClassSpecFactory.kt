package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.SmugglerException
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.reflect.ClassReference
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf
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

    if (!spec.signature.isNullOrBlank()) {
      throw SmugglerException("Invalid AutoParcelable class ''{0}'', generic classes are not supported at the moment.", spec.type.className)
    }

    val constructor = clazz.constructorList.first {
      !Flags.IS_SECONDARY.get(it.flags)
    }

    val properties = constructor.valueParameterList.map { parameter ->
      val name = resolver.getName(parameter.name).identifier
      val property = clazz.propertyList.first { resolver.getName(it.name).identifier == name }

      if (Flags.VISIBILITY.get(property.flags) != ProtoBuf.Visibility.PUBLIC) {
        throw SmugglerException("Invalid AutoParcelable class ''{0}'', only public properties are supported at the moment, but ''{1}'' has ''{2}'' visibility.",
            spec.type.className, resolver.getName(property.name).identifier, Flags.VISIBILITY.get(property.flags))
      }

      val getter = spec.getDeclaredMethod("get${name.capitalize()}")!!
      val type = getter.returns

      DataPropertySpec(name, null, type, getter)
    }

    return DataClassSpec(spec, properties)
  }
}
