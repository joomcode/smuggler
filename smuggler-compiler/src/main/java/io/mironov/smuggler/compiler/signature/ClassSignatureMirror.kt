package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.signature.SignatureReader
import java.util.ArrayList

internal interface ClassSignatureMirror {
  companion object {
    fun read(signature: String): ClassSignatureMirror {
      return ClassSignatureReader().run {
        SignatureReader(signature).accept(this)
        toClassSignature()
      }
    }
  }

  val typeParameters: List<TypeParameter>
  val superType: GenericType
  val interfaces: List<GenericType>

  class Builder() {
    var superType: GenericType = GenericType.RawType(Types.OBJECT)
    val typeParameters = ArrayList<TypeParameter.Builder>()
    val interfaces = ArrayList<GenericType>()

    fun build(): ClassSignatureMirror = ClassSignatureMirrorImpl(this)

    private class ClassSignatureMirrorImpl(builder: Builder) : ClassSignatureMirror {
      override val typeParameters = ArrayList(builder.typeParameters.map { it.build() })
      override val superType = builder.superType
      override val interfaces = builder.interfaces
    }
  }
}
