package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader
import java.util.concurrent.atomic.AtomicReference

sealed class GenericType {
  companion object {
    fun read(signature: String): GenericType {
      val result = AtomicReference<GenericType>()

      SignatureReader(signature).accept(GenericTypeReader {
        result.set(it)
      })

      return result.get()
    }
  }

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

val OBJECT_RAW_TYPE = GenericType.RawType(Types.OBJECT)
