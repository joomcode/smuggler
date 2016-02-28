package io.mironov.smuggler.compiler.common

inline fun <T, R> Iterable<T>.collect(collection: R, collector: (R, T) -> Unit): R {
  return fold(collection) { accumulator, value ->
    collector(accumulator, value)
    accumulator
  }
}

inline fun <T, R> Sequence<T>.collect(collection: R, collector: (R, T) -> Unit): R {
  return fold(collection) { accumulator, value ->
    collector(accumulator, value)
    accumulator
  }
}

inline fun <T, R> Array<T>.collect(collection: R, collector: (R, T) -> Unit): R {
  return fold(collection) { accumulator, value ->
    collector(accumulator, value)
    accumulator
  }
}
