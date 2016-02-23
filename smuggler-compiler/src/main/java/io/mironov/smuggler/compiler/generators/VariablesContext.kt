package io.mironov.smuggler.compiler.generators

import java.util.HashMap
import java.util.NoSuchElementException

internal class VariablesContext() {
  private val names = HashMap<String, Int>()

  fun variable(name: String, index: Int) {
    names[name] = index
  }

  fun variable(name: String): Int {
    return names[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }

  fun self(index: Int) = variable("self", index)
  fun self() = variable("self")

  fun value(index: Int) = variable("value", index)
  fun value(): Int = variable("value")

  fun parcel(index: Int) = variable("parcel", index)
  fun parcel(): Int = variable("parcel")

  fun flags(index: Int) = variable("flags", index)
  fun flags(): Int = variable("flags")
}
