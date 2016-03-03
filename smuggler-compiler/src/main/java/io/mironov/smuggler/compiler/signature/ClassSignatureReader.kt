package io.mironov.smuggler.compiler.signature

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class ClassSignatureReader : SignatureVisitor(Opcodes.ASM5) {
  private val builder = ClassSignatureMirror.Builder()
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

  override fun visitSuperclass(): SignatureVisitor {
    return GenericTypeReader {
      builder.superType(it)
    }
  }

  override fun visitInterface(): SignatureVisitor {
    return GenericTypeReader {
      builder.addInterface(it)
    }
  }

  fun toClassSignature(): ClassSignatureMirror {
    return builder.build()
  }
}
