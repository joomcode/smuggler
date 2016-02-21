package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.reflect.ClassSpec
import org.objectweb.asm.Type

internal data class DataClassSpec(
    val clazz: ClassSpec,
    val properties: List<DataPropertySpec>
)

internal data class DataPropertySpec(
    val name: String,
    val type: Type,
    val signature: String?
)
