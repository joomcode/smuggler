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
    private var returnType: GenericType? = null
    private val typeParameters = ArrayList<TypeParameter.Builder>()
    private val parameterTypes = ArrayList<GenericType>()
    private val exceptionTypes = ArrayList<GenericType>()

    fun addTypeParameterBuilder(builder: TypeParameter.Builder) = apply {
      typeParameters += builder
    }

    fun addParameterType(parameterType: GenericType) = apply {
      parameterTypes += parameterType
    }

    fun returnType(returnType: GenericType) = apply {
      this.returnType = returnType
    }

    fun addExceptionType(exceptionType: GenericType) = apply {
      exceptionTypes += exceptionType
    }

    fun build(): MethodSignatureMirror {
      return MethodSignatureMirrorImpl(this)
    }

    private class MethodSignatureMirrorImpl(builder: Builder) : MethodSignatureMirror {
      override val returnType: GenericType = builder.returnType!!
      override val typeParameters: List<TypeParameter> = ArrayList(builder.typeParameters.map { it.build() })
      override val parameterTypes: List<GenericType> = ArrayList(builder.parameterTypes)
      override val exceptionTypes: List<GenericType> = ArrayList(builder.exceptionTypes)
    }
  }
}
