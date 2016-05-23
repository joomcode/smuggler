package io.mironov.smuggler.compiler.annotations

import io.michaelrocks.grip.mirrors.Type

@AnnotationDelegate("io.mironov.smuggler.GlobalAdapter")
interface GlobalAdapter

@AnnotationDelegate("io.mironov.smuggler.LocalAdapter")
interface LocalAdapter {
  fun value(): Array<Type>
}
