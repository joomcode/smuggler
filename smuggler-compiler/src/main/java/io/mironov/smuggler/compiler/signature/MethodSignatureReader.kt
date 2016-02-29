package io.mironov.smuggler.compiler.signature

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class MethodSignatureReader : SignatureVisitor(Opcodes.ASM5) {
  private val builder = MethodSignatureMirror.Builder()

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

  override fun visitParameterType(): SignatureVisitor {
    return GenericTypeReader {
      builder.parameterTypes += it
    }
  }

  override fun visitReturnType(): SignatureVisitor {
    return GenericTypeReader {
      builder.returnType = it
    }
  }

  override fun visitExceptionType(): SignatureVisitor {
    return GenericTypeReader {
      builder.exceptionTypes += it
    }
  }

  fun toMethodSignature(): MethodSignatureMirror = builder.build()
}
