package io.mironov.smuggler.compiler.common

internal inline fun <T> given(condition: Boolean, body: () -> T): T? {
  return if (!condition) null else {
    body()
  }
}

internal inline fun <reified T : Any> Any?.cast(): T {
  return this as T
}
