package io.mironov.smuggler.compiler.signature

import io.mironov.smuggler.compiler.common.Types
import java.util.ArrayList

internal interface TypeParameter {
  val name: String
  val classBound: GenericType
  val interfaceBounds: List<GenericType>

  class Builder(val name: String) {
    var classBound: GenericType = GenericType.RawType(Types.OBJECT)
    val interfaceBounds = ArrayList<GenericType>()

    fun build(): TypeParameter = TypeParameterImpl(this)

    private class TypeParameterImpl(builder: Builder) : TypeParameter {
      override val name = builder.name
      override val classBound = builder.classBound
      override val interfaceBounds = ArrayList(builder.interfaceBounds)
    }
  }
}
