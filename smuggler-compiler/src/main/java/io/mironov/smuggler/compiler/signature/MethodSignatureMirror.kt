package io.mironov.smuggler.compiler.signature

import org.objectweb.asm.signature.SignatureReader
import java.util.ArrayList

internal interface MethodSignatureMirror {
  companion object {
    fun read(signature: String): MethodSignatureMirror {
      return MethodSignatureReader().run {
        SignatureReader(signature).accept(this)
        toMethodSignature()
      }
    }
  }

  val returnType: GenericType
  val typeParameters: List<TypeParameter>
  val parameterTypes: List<GenericType>
  val exceptionTypes: List<GenericType>

  class Builder() {
    var returnType: GenericType? = null
    val typeParameters = ArrayList<TypeParameter.Builder>()
    val parameterTypes = ArrayList<GenericType>()
    val exceptionTypes = ArrayList<GenericType>()

    fun build(): MethodSignatureMirror = MethodSignatureMirrorImpl(this)

    private class MethodSignatureMirrorImpl(builder: Builder) : MethodSignatureMirror {
      override val returnType: GenericType = builder.returnType!!
      override val typeParameters: List<TypeParameter> = ArrayList(builder.typeParameters.map { it.build() })
      override val parameterTypes: List<GenericType> = ArrayList(builder.parameterTypes)
      override val exceptionTypes: List<GenericType> = ArrayList(builder.exceptionTypes)
    }
  }
}
