package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.cast
import org.objectweb.asm.signature.SignatureReader
import java.util.*

internal interface ClassSignatureMirror {
  companion object {
    fun read(signature: String): ClassSignatureMirror {
      return ClassSignatureReader().run {
        SignatureReader(signature).accept(this)
        toClassSignature()
      }
    }
  }

  val superType: GenericType
  val typeParameters: List<TypeParameter>
  val interfaces: List<GenericType>

  class Builder() {
    private val typeParameters = ArrayList<TypeParameter.Builder>()
    private var superType = GenericType.RawType(Types.OBJECT).cast<GenericType>()
    private val interfaces = ArrayList<GenericType>()

    fun addTypeParameterBuilder(builder: TypeParameter.Builder) = apply {
      typeParameters += builder
    }

    fun superType(superType: GenericType) = apply {
      this.superType = superType
    }

    fun addInterface(interfaceType: GenericType) = apply {
      interfaces += interfaceType
    }

    fun build(): ClassSignatureMirror {
      return ClassSignatureMirrorImpl(this)
    }

    private class ClassSignatureMirrorImpl(builder: Builder) : ClassSignatureMirror {
      override val superType = builder.superType
      override val typeParameters = builder.typeParameters.mapTo(ArrayList()) { it.build() }
      override val interfaces = ArrayList(builder.interfaces)
    }
  }
}
