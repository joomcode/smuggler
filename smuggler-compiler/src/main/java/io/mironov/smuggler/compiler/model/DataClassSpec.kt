package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.reflect.ClassSpec

internal data class DataClassSpec(
    val clazz: ClassSpec,
    val fields: List<String>
)
