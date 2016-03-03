package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.cast
import java.util.ArrayList

internal interface TypeParameter {
  val name: String
  val classBound: GenericType
  val interfaceBounds: List<GenericType>

  class Builder(val name: String) {
    private var classBound = GenericType.RawType(Types.OBJECT).cast<GenericType>()
    private val interfaceBounds = ArrayList<GenericType>()

    fun classBound(classBound: GenericType) = apply {
      this.classBound = classBound
    }

    fun addInterfaceBound(interfaceBound: GenericType) = apply {
      interfaceBounds += interfaceBound
    }

    fun build(): TypeParameter {
      return TypeParameterImpl(this)
    }

    private class TypeParameterImpl(builder: Builder) : TypeParameter {
      override val name = builder.name
      override val interfaceBounds = ArrayList(builder.interfaceBounds)
      override val classBound = builder.classBound
    }
  }
}
