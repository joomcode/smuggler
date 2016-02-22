package io.mironov.smuggler.compiler.common

inline fun <T> given(condition: Boolean, body: () -> T): T? {
  if (condition) {
    return body()
  } else {
    return null
  }
}
