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

  fun self(index: Int) = variable("__smuggler__self__", index)
  fun self() = variable("__smuggler__self__")

  fun value(index: Int) = variable("__smuggler__value__", index)
  fun value() = variable("__smuggler__value__")

  fun property(name: String, index: Int) = variable("__smuggler__property__${name}__", index)
  fun property(name: String) = variable("__smuggler__property__${name}__")

  fun parcel(index: Int) = variable("__smuggler__parcel__", index)
  fun parcel(): Int = variable("__smuggler__parcel__")

  fun flags(index: Int) = variable("__smuggler__flags__", index)
  fun flags(): Int = variable("__smuggler__flags__")
}
