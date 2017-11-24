package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.toAsmType
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.cast
import org.objectweb.asm.Type

internal sealed class AutoParcelableClassSpec(val clazz: ClassMirror, val properties: List<AutoParcelablePropertySpec>) {
  class Object(clazz: ClassMirror, val name: String) : AutoParcelableClassSpec(clazz, emptyList())
  class Data(clazz: ClassMirror, properties: List<AutoParcelablePropertySpec>) : AutoParcelableClassSpec(clazz, properties)
}

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: KotlinType
)

internal sealed class KotlinType {
  data class Raw(val type: Type, val nullable: Boolean) : KotlinType()
  data class Array(val elementType: KotlinType, val nullable: Boolean) : KotlinType()
  data class Parameterized(val type: Type, val typeArguments: List<KotlinType>, val nullable: Boolean) : KotlinType()
  data class Inner(val type: KotlinType, val owner: KotlinType, val nullable: Boolean) : KotlinType()
  data class UpperBounded(val type: KotlinType, val nullable: Boolean) : KotlinType()
  data class LowerBounded(val type: KotlinType, val nullable: Boolean) : KotlinType()
  data class TypeVariable(val name: String, val nullable: Boolean) : KotlinType()

  fun asRawType() = cast<Raw>()
  fun asArrayType() = cast<Array>()
  fun asParameterizedType() = cast<Parameterized>()
  fun asInnerType() = cast<Inner>()
  fun asUpperBoundedType() = cast<UpperBounded>()
  fun asLowerBoundedType() = cast<LowerBounded>()
  fun asTypeVariable() = cast<TypeVariable>()

  fun asAsmType(): Type {
    return when (this) {
      is Raw -> type
      is Array -> Types.getArrayType(elementType.asAsmType())
      is Parameterized -> type
      else -> throw UnsupportedOperationException()
    }
  }

  companion object {
    fun from(type: Type): KotlinType {
      return if (type.sort == Type.ARRAY) {
        KotlinType.Array(from(Types.getElementType(type)), true)
      } else {
        KotlinType.Raw(type, true)
      }
    }

    fun from(type: GenericType): KotlinType {
      return when (type) {
        is GenericType.Raw -> from(type.type.toAsmType())
        is GenericType.Array -> KotlinType.Array(from(type.elementType), true)
        is GenericType.Parameterized -> KotlinType.Parameterized(type.type.toAsmType(), type.typeArguments.map { from(it) }, true)
        is GenericType.Inner -> KotlinType.Inner(from(type.type), from(type.ownerType), true)
        is GenericType.UpperBounded -> KotlinType.UpperBounded(from(type.upperBound), true)
        is GenericType.LowerBounded -> KotlinType.LowerBounded(from(type.lowerBound), true)
        is GenericType.TypeVariable -> KotlinType.TypeVariable(type.name, true)
      }
    }
  }
}
