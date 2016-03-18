package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.signature.GenericType

internal data class AutoParcelableClassSpec(
    val clazz: ClassMirror,
    val properties: List<AutoParcelablePropertySpec>
)

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: GenericType,
    val getter: MethodMirror
)
