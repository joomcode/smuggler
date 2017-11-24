package io.mironov.smuggler.compiler.generators

import io.michaelrocks.grip.Grip
import io.mironov.smuggler.compiler.model.KotlinType
import java.util.HashMap
import java.util.NoSuchElementException

internal class ValueContext(
    val type: KotlinType,
    val grip: Grip
) {
  private val names = HashMap<String, Int>()

  fun typed(newType: KotlinType): ValueContext {
    return ValueContext(newType, grip).also {
      it.variables(names)
    }
  }

  fun variables(values: Map<String, Int>) {
    names.clear()
    names.putAll(values)
  }

  fun variable(name: String, index: Int) {
    names[name] = index
  }

  fun variable(name: String): Int {
    return names[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }

  fun value(index: Int) = variable("__smuggler__value__", index)
  fun value() = variable("__smuggler__value__")

  fun property(name: String, index: Int) = variable("__smuggler__property__${name}__", index)
  fun property(name: String) = variable("__smuggler__property__${name}__")

  fun parcel(index: Int) = variable("__smuggler__parcel__", index)
  fun parcel(): Int = variable("__smuggler__parcel__")

  fun flags(index: Int) = variable("__smuggler__flags__", index)
  fun flags(): Int = variable("__smuggler__flags__")
}
