package io.mironov.smuggler.compiler

import io.michaelrocks.grip.mirrors.MethodMirror
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal class ClassWriter(private val environment: GenerationEnvironment) : ClassWriter((ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS)) {
  override fun getCommonSuperClass(left: String, right: String): String {
    return Types.OBJECT.internalName
  }

  fun visit(access: Int, name: Type, signature: String? = null, parent: Type = Types.OBJECT, interfaces: Array<out Type> = emptyArray()) {
    visit(V1_6, access, name.internalName, signature, parent.internalName, interfaces.map { it.internalName }.toTypedArray())
  }

  fun visitField(access: Int, name: String, type: Type, signature: String? = null): FieldVisitor {
    return visitField(access, name, type.descriptor, signature, null)
  }

  fun newMethod(access: Int, method: Method, signature: String? = null, body: GeneratorAdapter.() -> Unit) {
    GeneratorAdapter(this, access, method, signature).apply {
      body().apply {
        returnValue()
        endMethod()
      }
    }
  }

  fun newMethod(access: Int, method: MethodMirror, body: GeneratorAdapter.() -> Unit) {
    GeneratorAdapter(this, access, Methods.get(method), null).apply {
      body().apply {
        returnValue()
        endMethod()
      }
    }
  }

  fun newMethod(method: MethodMirror, body: GeneratorAdapter.() -> Unit) {
    newMethod(method.access, method, body)
  }
}
