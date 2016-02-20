package io.mironov.smuggler.compiler.reflect

import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.util.ArrayList

internal class AnnotationSpecVisitor(
    private val type: Type,
    private val action: (AnnotationSpec) -> Unit
) : AnnotationVisitor(Opcodes.ASM5) {
  private val builder = AnnotationSpec.Builder(type)

  override fun visit(name: String, value: Any?) {
    builder.value(name, value)
  }

  override fun visitAnnotation(name: String, desc: String): AnnotationVisitor? {
    return if (Types.isSystemClass(Type.getType(desc))) null else {
      AnnotationSpecVisitor(Type.getType(desc)) {
        builder.value(name, it)
      }
    }
  }

  override fun visitArray(name: String): AnnotationVisitor? {
    return AnnotationSpecVisitor.ArraySpecVisitor {
      builder.value(name, it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }

  private class ArraySpecVisitor(private val action: (Array<Any>) -> Unit) : AnnotationVisitor(Opcodes.ASM5) {
    private val values = ArrayList<Any?>()

    override fun visit(name: String?, value: Any?) {
      values.add(value)
    }

    override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor? {
      return if (Types.isSystemClass(Type.getType(desc))) null else {
        AnnotationSpecVisitor(Type.getType(desc)) {
          values.add(it)
        }
      }
    }

    override fun visitArray(name: String): AnnotationVisitor? {
      return AnnotationSpecVisitor.ArraySpecVisitor() {
        values.add(it)
      }
    }

    override fun visitEnd() {
      action(values.toArray())
    }
  }
}
