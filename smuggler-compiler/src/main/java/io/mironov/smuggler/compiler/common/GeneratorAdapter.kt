package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.FieldSpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal class GeneratorAdapter(visitor: ClassVisitor, access: Int, method: Method, signature: String?) : org.objectweb.asm.commons.GeneratorAdapter(Opcodes.ASM5, visitor.visitMethod(access, method.name, method.descriptor, signature, null), access, method.name, method.descriptor) {
  fun pushNull() {
    visitInsn(Opcodes.ACONST_NULL)
  }

  fun checkCast(spec: ClassSpec) {
    checkCast(spec.type)
  }

  fun getField(owner: ClassSpec, field: FieldSpec) {
    getField(owner.type, field.name, field.type)
  }

  fun getField(owner: ClassSpec, name: String, type: ClassSpec) {
    getField(owner.type, name, type.type)
  }

  fun getField(owner: ClassSpec, name: String, type: Type) {
    getField(owner.type, name, type)
  }

  fun putField(owner: ClassSpec, field: FieldSpec) {
    putField(owner.type, field.name, field.type)
  }

  fun putField(owner: ClassSpec, name: String, type: ClassSpec) {
    putField(owner.type, name, type.type)
  }

  fun putField(owner: ClassSpec, name: String, type: Type) {
    putField(owner.type, name, type)
  }

  fun invokeStatic(owner: ClassSpec, method: Method) {
    invokeStatic(owner.type, method)
  }

  fun invokeStatic(owner: ClassSpec, method: MethodSpec) {
    invokeStatic(owner.type, Methods.get(method))
  }

  fun invokeVirtual(owner: ClassSpec, method: Method) {
    invokeVirtual(owner.type, method)
  }

  fun invokeVirtual(owner: ClassSpec, method: MethodSpec) {
    invokeVirtual(owner.type, Methods.get(method))
  }

  fun newInstance(type: Type, method: Method, args: () -> Unit = {}) {
    newInstance(type)
    dup()
    args()
    invokeConstructor(type, method)
  }
}
