package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.common.Opener
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class ClassSpecVisitor(
    private val access: Int,
    private val type: Type,
    private val parent: Type,
    private val interfaces: Collection<Type>,
    private val opener: Opener,
    private val action: (ClassSpec) -> Unit
) : ClassVisitor(Opcodes.ASM5) {
  private val builder = ClassSpec.Builder(access, type, parent, opener).interfaces(interfaces)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    return if (Types.isSystemClass(Type.getType(desc))) null else {
      AnnotationSpecVisitor(Type.getType(desc)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
    return MethodSpecVisitor(access, name, Type.getType(desc), signature) {
      builder.method(it)
    }
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
    return FieldSpecVisitor(access, name, Type.getType(desc)) {
      builder.field(it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
