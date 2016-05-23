package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.isStatic
import io.michaelrocks.grip.mirrors.toAsmType
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.getAnnotation
import io.mironov.smuggler.compiler.common.getDeclaredField
import io.mironov.smuggler.compiler.common.getDeclaredMethod
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal object AutoParcelableClassSpecFactory {
  fun from(mirror: ClassMirror): AutoParcelableClassSpec {
    val metadata = mirror.getAnnotation<Metadata>() ?: run {
      throw InvalidAutoParcelableException(mirror.type, "Only kotlin classes can implement AutoParcelable interface")
    }

    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
    val clazz = proto.classProto
    val resolver = proto.nameResolver

    if (Flags.CLASS_KIND.get(clazz.flags) == ProtoBuf.Class.Kind.OBJECT) {
      return AutoParcelableClassSpec.Object(mirror, "INSTANCE")
    }

    if (!Flags.IS_DATA.get(clazz.flags)) {
      throw InvalidAutoParcelableException(mirror.type, "Only data classes and objects can implement AutoParcelable interface")
    }

    if (!mirror.signature.typeParameters.isEmpty()) {
      throw InvalidAutoParcelableException(mirror.type, "Generic classes are not supported at the moment")
    }

    if (mirror.superType != null && mirror.superType?.toAsmType() != Types.OBJECT) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must be direct subclasses of java.lang.Object")
    }

    val creator = mirror.getDeclaredField("CREATOR")
    val constructor = clazz.constructorList.singleOrNull() { !Flags.IS_SECONDARY.get(it.flags) } ?: run {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must have exactly one primary constructor")
    }

    if (Flags.VISIBILITY.get(constructor.flags) != ProtoBuf.Visibility.PUBLIC) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must have primary constructor with public visibility")
    }

    if (creator != null && creator.isStatic) {
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes shouldn''t declare CREATOR field")
    }

    return AutoParcelableClassSpec.Data(mirror, constructor.valueParameterList.mapIndexed { index, parameter ->
      val name = resolver.getName(parameter.name).identifier
      val getter = mirror.getDeclaredMethod("component${index + 1}")!!

      AutoParcelablePropertySpec(name, getter.signature.returnType, getter)
    })
  }
}
