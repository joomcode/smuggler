package io.mironov.smuggler.compiler.common

inline fun <T> given(condition: Boolean, body: () -> T): T? {
  if (condition) {
    return body()
  } else {
    return null
  }
}

inline fun <reified T : Any> Any?.cast(): T {
  return this as T
}

inline fun <reified T : Any> Any?.castOptional(): T? {
  return this as? T
}
