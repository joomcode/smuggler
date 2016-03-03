package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import java.util.ArrayList

internal class GenericTypeReader(
    private val callback: (GenericType) -> Unit
) : SignatureVisitor(Opcodes.ASM5) {
  private var genericType: GenericType? = null
  private var classType: Type? = null
  private val typeArguments = ArrayList<GenericType>()
  private var arrayDimensions = 0

  override fun visitBaseType(descriptor: Char) {
    genericType = GenericType.RawType(Type.getType(descriptor.toString()))
    visitEnd()
  }

  override fun visitTypeVariable(name: String) {
    genericType = GenericType.TypeVariable(name)
    visitEnd()
  }

  override fun visitArrayType(): SignatureVisitor {
    ++arrayDimensions
    return this
  }

  override fun visitClassType(name: String) {
    classType = Type.getObjectType(name)
    typeArguments.clear()
  }

  override fun visitInnerClassType(name: String) {
    buildGenericType()
    classType = Type.getObjectType(name)
    typeArguments.clear()
  }

  override fun visitTypeArgument() {
    typeArguments.add(GenericType.UpperBoundedType(listOf(GenericType.RawType(Types.OBJECT))))
  }

  override fun visitTypeArgument(name: Char): SignatureVisitor {
    return GenericTypeReader {
      typeArguments.add(when (name) {
        EXTENDS -> GenericType.UpperBoundedType(listOf(it))
        SUPER -> GenericType.LowerBoundedType(it)
        INSTANCEOF -> it
        else -> error("Unknown wildcard type: $name")
      })
    }
  }

  override fun visitEnd() {
    buildGenericType()
    callback(genericType!!)
  }

  private fun buildGenericType() {
    if (classType != null) {
      val innerType = if (typeArguments.isEmpty()) {
        GenericType.RawType(classType!!)
      } else {
        GenericType.ParameterizedType(classType!!, typeArguments.toList())
      }
      genericType = genericType?.let { GenericType.InnerType(innerType, it) } ?: innerType
    }

    while (arrayDimensions > 0) {
      genericType = GenericType.ArrayType(genericType!!)
      --arrayDimensions
    }
  }
}

internal fun readGenericType(signature: String): GenericType {
  var genericType: GenericType? = null
  SignatureReader(signature).acceptType(GenericTypeReader {
    genericType = it
  })
  return genericType!!
}
