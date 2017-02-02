package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.signature.GenericType

internal sealed class AutoParcelableClassSpec(val clazz: ClassMirror, val properties: List<AutoParcelablePropertySpec>) {
  class Object(clazz: ClassMirror, val name: String) : AutoParcelableClassSpec(clazz, emptyList())
  class Data(clazz: ClassMirror, properties: List<AutoParcelablePropertySpec>) : AutoParcelableClassSpec(clazz, properties)
}

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: GenericType
)
