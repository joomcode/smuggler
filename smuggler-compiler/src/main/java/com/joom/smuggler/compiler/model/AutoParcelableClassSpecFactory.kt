package com.joom.smuggler.compiler.model

import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.isStatic
import com.joom.grip.mirrors.signature.GenericType
import com.joom.grip.mirrors.toAsmType
import com.joom.smuggler.compiler.InvalidAutoParcelableException
import com.joom.smuggler.compiler.annotations.Metadata
import com.joom.smuggler.compiler.annotations.data
import com.joom.smuggler.compiler.annotations.strings
import com.joom.smuggler.compiler.common.Types
import com.joom.smuggler.compiler.common.getAnnotation
import com.joom.smuggler.compiler.common.getDeclaredField
import com.joom.smuggler.compiler.common.given
import org.objectweb.asm.Type
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Class
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Visibility
import kotlin.reflect.jvm.internal.impl.metadata.deserialization.Flags
import kotlin.reflect.jvm.internal.impl.metadata.jvm.deserialization.JvmProtoBufUtil

internal object AutoParcelableClassSpecFactory {
  fun from(mirror: ClassMirror): AutoParcelableClassSpec {
    val metadata = mirror.getAnnotation<Metadata>() ?: run {
      throw InvalidAutoParcelableException(mirror.type, "Only kotlin classes can implement AutoParcelable interface")
    }

    val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
    val resolver = proto.component1()
    val clazz = proto.component2()

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

    if (!mirror.signature.typeVariables.isEmpty()) {
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
      throw InvalidAutoParcelableException(mirror.type, "AutoParcelable classes must not declare CREATOR field")
    }

    return AutoParcelableClassSpec.Data(mirror, constructor.valueParameterList.mapIndexed { _, parameter ->
      val name = resolver.getString(parameter.name)

      val field = mirror.getDeclaredField(name) ?: run {
        throw InvalidAutoParcelableException(mirror.type, "Unable to find field \"$name\". Make sure to declare the property as val or var.")
      }

      AutoParcelablePropertySpec(name, createKotlinType(field.signature.type, parameter.type))
    })
  }

  private fun createKotlinType(generic: GenericType, proto: ProtoBuf.Type?): KotlinType {
    return when (generic) {
      is GenericType.Raw -> createKotlinType(
        type = generic.type.toAsmType(),
        proto = proto
      )

      is GenericType.Array -> KotlinType.Array(
        elementType = createKotlinType(generic.elementType, argument(proto, 0, 1)),
        nullable = nullable(proto)
      )

      is GenericType.Parameterized -> KotlinType.Parameterized(
        type = generic.type.toAsmType(),
        nullable = nullable(proto),
        typeArguments = generic.typeArguments.mapIndexed { index, argument ->
          createKotlinType(argument, argument(proto, index, generic.typeArguments.size))
        }
      )

      is GenericType.Inner -> KotlinType.Inner(
        type = createKotlinType(generic.type, null),
        owner = createKotlinType(generic.ownerType, null),
        nullable = nullable(proto)
      )

      is GenericType.UpperBounded -> KotlinType.UpperBounded(
        type = createKotlinType(generic.upperBound, null),
        nullable = nullable(proto)
      )

      is GenericType.LowerBounded -> KotlinType.LowerBounded(
        type = createKotlinType(generic.lowerBound, null),
        nullable = nullable(proto)
      )

      is GenericType.TypeVariable -> KotlinType.TypeVariable(
        name = generic.name,
        nullable = nullable(proto)
      )
    }
  }

  private fun createKotlinType(type: Type, proto: ProtoBuf.Type?): KotlinType {
    return if (type.sort == Type.ARRAY) {
      KotlinType.Array(createKotlinType(Types.getElementType(type), argument(proto, 0, 1)), nullable(proto))
    } else {
      KotlinType.Raw(type, nullable(proto))
    }
  }

  private fun nullable(proto: ProtoBuf.Type?): Boolean {
    return proto == null || proto.nullable
  }

  private fun argument(proto: ProtoBuf.Type?, index: Int, expected: Int): ProtoBuf.Type? {
    return given(proto != null && proto.argumentCount == expected) {
      proto!!.argumentList.getOrNull(index)?.type
    }
  }
}
