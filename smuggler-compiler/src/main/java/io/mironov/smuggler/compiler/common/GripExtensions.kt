package io.mironov.smuggler.compiler.common

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.signature.GenericType
import org.objectweb.asm.Type

internal fun GenericType.asRawType() = cast<GenericType.RawType>()
internal fun GenericType.asTypeVariable() = cast<GenericType.TypeVariable>()
internal fun GenericType.asGenericArrayType() = cast<GenericType.GenericArrayType>()
internal fun GenericType.asParameterizedType() = cast<GenericType.ParameterizedType>()
internal fun GenericType.asInnerType() = cast<GenericType.InnerType>()
internal fun GenericType.asUpperBoundedType() = cast<GenericType.UpperBoundedType>()
internal fun GenericType.asLowerBoundedType() = cast<GenericType.LowerBoundedType>()

internal fun GenericType.asAsmType(): Type {
  return when (this) {
    is GenericType.RawType -> type
    is GenericType.GenericArrayType -> Types.getArrayType(elementType.asAsmType())
    is GenericType.ParameterizedType -> type
    else -> throw UnsupportedOperationException()
  }
}

internal fun ClassRegistry.isSubclassOf(type: Type, parent: Type): Boolean {
  return false
}
