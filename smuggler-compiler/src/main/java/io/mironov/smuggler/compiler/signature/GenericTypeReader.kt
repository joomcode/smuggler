package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureVisitor
import java.util.ArrayList

internal class GenericTypeReader(private val callback: (GenericType) -> Unit) : SignatureVisitor(Opcodes.ASM5) {
  private var genericType: GenericType? = null
  private var classType: Type? = null
  private var typeArguments = ArrayList<GenericType>()

  override fun visitBaseType(descriptor: Char) {
    genericType = GenericType.RawType(Type.getType(descriptor.toString()))
  }

  override fun visitTypeVariable(name: String) {
    genericType = GenericType.TypeVariable(name)
  }

  override fun visitArrayType(): SignatureVisitor {
    return GenericTypeReader {
      genericType = GenericType.GenericArrayType(it)
    }
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
    callback(buildGenericType())
  }

  private fun buildGenericType(): GenericType {
    if (typeArguments.isEmpty()) {
      genericType = GenericType.RawType(classType!!)
    } else {
      genericType = GenericType.ParameterizedType(classType!!, genericType, typeArguments.toList())
    }

    return genericType!!
  }
}
