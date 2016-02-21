package io.mironov.smuggler.compiler.reflect

import org.objectweb.asm.Type

internal data class Signature private constructor(private val signature: String) {
  companion object {
    fun simple(type: Type): Signature {
      return Signature("L${type.internalName};")
    }

    fun type(parent: Signature, vararg interfaces: Signature): Signature {
      return Signature("${parent.signature}${interfaces.map(Signature::signature).joinToString("")}")
    }

    fun generic(raw: Type, vararg args: Type): Signature {
      return Signature("L${raw.internalName}<${args.map { simple(it).signature }.joinToString("")}>;")
    }
  }

  override fun toString(): String {
    return signature
  }
}
