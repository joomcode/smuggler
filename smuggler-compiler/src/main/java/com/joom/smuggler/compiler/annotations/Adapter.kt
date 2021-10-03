package com.joom.smuggler.compiler.annotations

import com.joom.grip.mirrors.Type

@AnnotationDelegate("com.joom.smuggler.GlobalAdapter")
internal interface GlobalAdapter

@AnnotationDelegate("com.joom.smuggler.LocalAdapter")
internal interface LocalAdapter {
  fun value(): Array<Type>
}
