package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class MethodSpecVisitor(
    private val access: Int,
    private val name: String,
    private val type: Type,
    private val signature: String?,
    private val action: (MethodSpec) -> Unit
) : MethodVisitor(Opcodes.ASM5) {
  private val builder = MethodSpec.Builder(access, name, type, signature)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    return if (Types.isSystemClass(Type.getType(desc))) null else {
      AnnotationSpecVisitor(Type.getType(desc)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
