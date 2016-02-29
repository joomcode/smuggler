package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.cast
import org.objectweb.asm.Type

internal sealed class GenericType() {
  val raw: Type
    get() = when (this) {
      is RawType -> type
      is ParameterizedType -> type
      else -> throw UnsupportedOperationException()
    }

  fun asRawType() = cast<RawType>()
  fun asTypeVariable() = cast<TypeVariable>()
  fun asGenericArrayType() = cast<GenericArrayType>()
  fun asParameterizedType() = cast<ParameterizedType>()
  fun asUpperBoundedType() = cast<UpperBoundedType>()
  fun asLowerBoundedType() = cast<LowerBoundedType>()

  class RawType(val type: Type) : GenericType() {
    override fun toString(): String = "RawType($type)"
  }

  class TypeVariable(val name: String) : GenericType() {
    override fun toString(): String = "TypeVariable($name)"
  }

  class GenericArrayType(val elementType: GenericType) : GenericType() {
    override fun toString(): String = "GenericArray($elementType)"
  }

  class ParameterizedType(
      val type: Type,
      val ownerType: GenericType?,
      val typeArguments: List<GenericType>
  ) : GenericType() {
    override fun toString(): String {
      return buildString {
        append("ParameterizedType(")
        append(type)
        append(", ")
        append(ownerType)
        append(", ")
        typeArguments.joinTo(this)
        append(")")
      }
    }
  }

  class UpperBoundedType(val upperBounds: List<GenericType>) : GenericType() {
    override fun toString(): String = upperBounds.joinToString(prefix = "UpperBoundedType(", postfix = ")")
  }

  class LowerBoundedType(val lowerBound: GenericType) : GenericType() {
    override fun toString(): String = "LowerBoundedType($lowerBound)"
  }
}
