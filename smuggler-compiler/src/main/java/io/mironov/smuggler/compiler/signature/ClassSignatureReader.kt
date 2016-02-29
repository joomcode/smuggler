package io.mironov.smuggler.compiler.signature

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class ClassSignatureReader : SignatureVisitor(Opcodes.ASM5) {
  private val builder = ClassSignatureMirror.Builder()

  override fun visitFormalTypeParameter(name: String) {
    builder.typeParameters += TypeParameter.Builder(name)
  }

  override fun visitClassBound(): SignatureVisitor {
    return GenericTypeReader {
      builder.typeParameters.last().classBound = it
    }
  }

  override fun visitInterfaceBound(): SignatureVisitor {
    return GenericTypeReader {
      builder.typeParameters.last().interfaceBounds += it
    }
  }

  override fun visitSuperclass(): SignatureVisitor {
    return GenericTypeReader {
      builder.superType = it
    }
  }

  override fun visitInterface(): SignatureVisitor {
    return GenericTypeReader {
      builder.interfaces += it
    }
  }

  fun toClassSignature(): ClassSignatureMirror = builder.build()
}
