package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.common.Opener
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.util.concurrent.atomic.AtomicReference

internal data class ClassReference(
    val access: Int,
    val type: Type,
    val parent: Type,
    val interfaces: Collection<Type>,
    val opener: Opener
) {
  fun resolve(): ClassSpec {
    val flags = ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES

    val reader = ClassReader(opener.open())
    val result = AtomicReference<ClassSpec>()

    reader.accept(ClassSpecVisitor(access, type, parent, interfaces, opener) {
      result.set(it)
    }, flags)

    return result.get()
  }
}
