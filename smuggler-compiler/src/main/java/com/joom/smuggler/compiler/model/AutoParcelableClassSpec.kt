package com.joom.smuggler.compiler.model

import com.joom.grip.mirrors.ClassMirror
import com.joom.smuggler.compiler.common.Types
import com.joom.smuggler.compiler.common.cast
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
  data class Raw(val type: Type, override val nullable: Boolean) : KotlinType()
  data class Array(val elementType: KotlinType, override val nullable: Boolean) : KotlinType()
  data class Parameterized(val type: Type, val typeArguments: List<KotlinType>, override val nullable: Boolean) : KotlinType()
  data class Inner(val type: KotlinType, val owner: KotlinType, override val nullable: Boolean) : KotlinType()
  data class UpperBounded(val type: KotlinType, override val nullable: Boolean) : KotlinType()
  data class LowerBounded(val type: KotlinType, override val nullable: Boolean) : KotlinType()
  data class TypeVariable(val name: String, override val nullable: Boolean) : KotlinType()

  abstract val nullable: Boolean

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
}
