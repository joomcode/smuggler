package io.mironov.smuggler.compiler.annotations

import io.michaelrocks.grip.mirrors.Type

@AnnotationDelegate("io.mironov.smuggler.GlobalAdapter")
internal interface GlobalAdapter

@AnnotationDelegate("io.mironov.smuggler.LocalAdapter")
internal interface LocalAdapter {
  fun value(): Array<Type>
}
