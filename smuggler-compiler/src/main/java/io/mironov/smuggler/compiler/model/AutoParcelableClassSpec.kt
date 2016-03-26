package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.signature.GenericType

internal sealed class AutoParcelableClassSpec(val clazz: ClassMirror) {
  class Object(clazz: ClassMirror, val name: String) : AutoParcelableClassSpec(clazz)
  class Data(clazz: ClassMirror, val properties: List<AutoParcelablePropertySpec>) : AutoParcelableClassSpec(clazz)
}

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: GenericType,
    val getter: MethodMirror
)
