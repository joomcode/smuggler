package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.cast
import org.objectweb.asm.Type

internal sealed class GenericType {
  fun asRawType() = cast<RawType>()
  fun asTypeVariable() = cast<TypeVariable>()
  fun asGenericArrayType() = cast<ArrayType>()
  fun asParameterizedType() = cast<ParameterizedType>()
  fun asInnerType() = cast<InnerType>()
  fun asUpperBoundedType() = cast<UpperBoundedType>()
  fun asLowerBoundedType() = cast<LowerBoundedType>()

  fun asAsmType(): Type {
    return when (this) {
      is RawType -> type
      is ArrayType -> Types.getArrayType(elementType.asAsmType())
      is ParameterizedType -> type
      else -> throw UnsupportedOperationException()
    }
  }

  class RawType(val type: Type) : GenericType() {
    override fun toString(): String = type.className
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type }
    override fun hashCode(): Int = 31 + type.hashCode()
  }

  class TypeVariable(val name: String) : GenericType() {
    override fun toString(): String = name
    override fun equals(other: Any?): Boolean = equals(other) { name == it.name }
    override fun hashCode(): Int = 31 + name.hashCode()
  }

  class ArrayType(val elementType: GenericType) : GenericType() {
    override fun toString(): String = "$elementType[]"
    override fun equals(other: Any?): Boolean = equals(other) { elementType == it.elementType }
    override fun hashCode(): Int = 31 + elementType.hashCode()
  }

  class ParameterizedType(val type: Type, val typeArguments: List<GenericType>) : GenericType() {
    override fun toString() = StringBuilder("${type.className}").apply { typeArguments.joinTo(this, prefix = "<", postfix = ">") }.toString()
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type && typeArguments == it.typeArguments }
    override fun hashCode(): Int = 31 * (31 + type.hashCode()) + typeArguments.hashCode()
  }

  class InnerType(val type: GenericType, val ownerType: GenericType) : GenericType() {
    override fun toString(): String = "$ownerType.$type"
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type && ownerType == it.ownerType }
    override fun hashCode(): Int = 31 * (31 + type.hashCode()) + ownerType.hashCode()
  }

  class UpperBoundedType(val upperBounds: List<GenericType>) : GenericType() {
    override fun toString(): String = upperBounds.joinToString(prefix = "? extends ", separator = " & ")
    override fun equals(other: Any?): Boolean = equals(other) { upperBounds == it.upperBounds }
    override fun hashCode(): Int = 31 + upperBounds.hashCode()
  }

  class LowerBoundedType(val lowerBound: GenericType) : GenericType() {
    override fun toString(): String = "? super $lowerBound"
    override fun equals(other: Any?): Boolean = equals(other) { lowerBound == it.lowerBound }
    override fun hashCode(): Int = 31 + lowerBound.hashCode()
  }
}

private inline fun <reified T : Any> T.equals(other: Any?, body: (T) -> Boolean): Boolean {
  if (this === other) {
    return true
  }

  val that = other as? T ?: return false
  return body(that)
}
