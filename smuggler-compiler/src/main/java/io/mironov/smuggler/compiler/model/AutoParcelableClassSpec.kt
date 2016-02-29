package io.mironov.smuggler.compiler.model

import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import io.mironov.smuggler.compiler.signature.GenericType
import org.objectweb.asm.Type

internal data class AutoParcelableClassSpec(
    val clazz: ClassSpec,
    val properties: List<AutoParcelablePropertySpec>
)

internal data class AutoParcelablePropertySpec(
    val name: String,
    val type: Type,
    val generic: GenericType?,
    val getter: MethodSpec
)
