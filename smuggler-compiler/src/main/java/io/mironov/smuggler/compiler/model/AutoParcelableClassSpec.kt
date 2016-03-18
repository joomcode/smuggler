package io.mironov.smuggler.compiler.model

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.MethodSpec

internal data class AutoParcelableClassSpec(
    val clazz: ClassSpec,
    val properties: List<AutoParcelablePropertySpec>
)

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: GenericType,
    val getter: MethodSpec
)
