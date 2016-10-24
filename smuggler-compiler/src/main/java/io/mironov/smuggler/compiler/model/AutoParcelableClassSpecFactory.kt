package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.isStatic
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.common.getAnnotation
import io.mironov.smuggler.compiler.common.getDeclaredField
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf.Class
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf.Visibility
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal object AutoParcelableClassSpecFactory {
  fun from(mirror: ClassMirror): AutoParcelableClassSpec {
    val metadata = mirror.getAnnotation<Metadata>() ?: run {
      throw InvalidAutoParcelableException(mirror.type, "Only kotlin classes can implement AutoParcelable interface")
    }

    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
    val clazz = proto.classProto
    val resolver = proto.nameResolver

    if (Flags.CLASS_KIND.get(clazz.flags) == Class.Kind.OBJECT) {
      return AutoParcelableClassSpec.Object(mirror, "INSTANCE")
    }

    if (Flags.CLASS_KIND.get(clazz.flags) == Class.Kind.COMPANION_OBJECT) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable companion objects are not supported at the moment")
    }

    if (Flags.CLASS_KIND.get(clazz.flags) == Class.Kind.ANNOTATION_CLASS) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable annotations are not supported at the moment")
    }

    if (Flags.CLASS_KIND.get(clazz.flags) in arrayOf(Class.Kind.ENUM_CLASS, Class.Kind.ENUM_ENTRY)) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable enums are not supported at the moment")
    }

    if (!mirror.signature.typeParameters.isEmpty()) {
      throw InvalidAutoParcelableException(mirror.type, "Generic classes are not supported at the moment")
    }

    val creator = mirror.getDeclaredField("CREATOR")
    val constructor = clazz.constructorList.singleOrNull { !Flags.IS_SECONDARY.get(it.flags) } ?: run {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must have exactly one primary constructor")
    }

    if (Flags.VISIBILITY.get(constructor.flags) != Visibility.PUBLIC) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must have primary constructor with public visibility")
    }

    if (creator != null && creator.isStatic) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes shouldn''t declare CREATOR field")
    }

    return AutoParcelableClassSpec.Data(mirror, constructor.valueParameterList.mapIndexed { index, parameter ->
      val name = resolver.getName(parameter.name).identifier

      val field = mirror.getDeclaredField(name) ?: run {
        throw InvalidAutoParcelableException(mirror.type, "Unable to find field \"$name\". Make sure to declare the property as val or var.")
      }

      AutoParcelablePropertySpec(name, field.signature.type)
    })
  }
}
