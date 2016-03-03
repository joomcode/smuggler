package io.mironov.smuggler.compiler.signature

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class MethodSignatureReader : SignatureVisitor(Opcodes.ASM5) {
  private val builder = MethodSignatureMirror.Builder()
  private var typeParameterBuilder: TypeParameter.Builder? = null

  override fun visitFormalTypeParameter(name: String) {
    typeParameterBuilder = TypeParameter.Builder(name).apply {
      builder.addTypeParameterBuilder(this)
    }
  }

  override fun visitClassBound(): SignatureVisitor {
    return GenericTypeReader {
      typeParameterBuilder!!.classBound(it)
    }
  }

  override fun visitInterfaceBound(): SignatureVisitor {
    return GenericTypeReader {
      typeParameterBuilder!!.addInterfaceBound(it)
    }
  }

  override fun visitParameterType(): SignatureVisitor {
    return GenericTypeReader {
      builder.addParameterType(it)
    }
  }

  override fun visitReturnType(): SignatureVisitor {
    return GenericTypeReader {
      builder.returnType(it)
    }
  }

  override fun visitExceptionType(): SignatureVisitor {
    return GenericTypeReader {
      builder.addExceptionType(it)
    }
  }

  fun toMethodSignature(): MethodSignatureMirror {
    return builder.build()
  }
}
