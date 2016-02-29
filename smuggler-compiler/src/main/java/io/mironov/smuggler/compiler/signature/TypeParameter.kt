package io.mironov.smuggler.compiler.signature

import java.util.ArrayList

interface TypeParameter {
  val name: String
  val classBound: GenericType
  val interfaceBounds: List<GenericType>

  class Builder(val name: String) {
    var classBound: GenericType = OBJECT_RAW_TYPE
    val interfaceBounds = ArrayList<GenericType>()

    fun build(): TypeParameter = TypeParameterImpl(this)

    private class TypeParameterImpl(builder: Builder) : TypeParameter {
      override val name = builder.name
      override val classBound = builder.classBound
      override val interfaceBounds = ArrayList(builder.interfaceBounds)
    }
  }
}
