package io.mironov.smuggler.compiler.common

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal open class GeneratorAdapter : org.objectweb.asm.commons.GeneratorAdapter {
  constructor(delegate: MethodVisitor?, access: Int, method: Method): super(Opcodes.ASM5, delegate, access, method.name, method.descriptor)
  constructor(visitor: ClassVisitor, access: Int, method: Method, signature: String?): super(Opcodes.ASM5, visitor.visitMethod(access, method.name, method.descriptor, signature, null), access, method.name, method.descriptor)

  fun pushNull() {
    visitInsn(Opcodes.ACONST_NULL)
  }

  fun add(type: Type) {
    visitInsn(type.getOpcode(Opcodes.IADD))
  }

  fun checkCast(spec: ClassMirror) {
    checkCast(spec.type)
  }

  fun getField(owner: ClassMirror, field: FieldMirror) {
    getField(owner.type, field.name, field.type)
  }

  fun getField(owner: ClassMirror, name: String, type: ClassMirror) {
    getField(owner.type, name, type.type)
  }

  fun getField(owner: ClassMirror, name: String, type: Type) {
    getField(owner.type, name, type)
  }

  fun putField(owner: ClassMirror, field: FieldMirror) {
    putField(owner.type, field.name, field.type)
  }

  fun putField(owner: ClassMirror, name: String, type: ClassMirror) {
    putField(owner.type, name, type.type)
  }

  fun putField(owner: ClassMirror, name: String, type: Type) {
    putField(owner.type, name, type)
  }

  fun invokeStatic(owner: ClassMirror, method: Method) {
    invokeStatic(owner.type, method)
  }

  fun invokeStatic(owner: ClassMirror, method: MethodMirror) {
    invokeStatic(owner.type, Methods.get(method))
  }

  fun invokeVirtual(owner: ClassMirror, method: Method) {
    invokeVirtual(owner.type, method)
  }

  fun invokeVirtual(owner: ClassMirror, method: MethodMirror) {
    invokeVirtual(owner.type, Methods.get(method))
  }

  fun invokeVirtual(owner: Type, method: MethodMirror) {
    invokeVirtual(owner, Methods.get(method))
  }

  fun invokeSpecial(owner: ClassMirror, method: Method) {
    visitMethodInsn(Opcodes.INVOKESPECIAL, owner.type.internalName, method.name, method.descriptor, false)
  }

  fun invokeSpecial(owner: ClassMirror, method: MethodMirror) {
    visitMethodInsn(Opcodes.INVOKESPECIAL, owner.type.internalName, method.name, method.type.descriptor, false)
  }

  fun invokeSpecial(owner: Type, method: MethodMirror) {
    visitMethodInsn(Opcodes.INVOKESPECIAL, owner.internalName, method.name, method.type.descriptor, false)
  }

  fun newInstance(type: Type, method: Method, args: () -> Unit = {}) {
    newInstance(type)
    dup()
    args()
    invokeConstructor(type, method)
  }
}
