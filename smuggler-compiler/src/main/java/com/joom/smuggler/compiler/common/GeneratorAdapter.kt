package com.joom.smuggler.compiler.common

import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.FieldMirror
import com.joom.grip.mirrors.MethodMirror
import com.joom.grip.mirrors.toAsmType
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal open class GeneratorAdapter : org.objectweb.asm.commons.GeneratorAdapter {
  constructor(delegate: MethodVisitor?, access: Int, method: Method) : super(Opcodes.ASM9, delegate, access, method.name, method.descriptor)
  constructor(visitor: ClassVisitor, access: Int, method: Method, signature: String?) : super(
    Opcodes.ASM9,
    visitor.visitMethod(access, method.name, method.descriptor, signature, null),
    access,
    method.name,
    method.descriptor
  )

  fun pushNull() {
    visitInsn(Opcodes.ACONST_NULL)
  }

  fun add(type: Type) {
    visitInsn(type.getOpcode(Opcodes.IADD))
  }

  fun checkCast(spec: ClassMirror) {
    checkCast(spec.type.toAsmType())
  }

  fun getField(owner: ClassMirror, field: FieldMirror) {
    getField(owner.type.toAsmType(), field.name, field.type.toAsmType())
  }

  fun getField(owner: ClassMirror, name: String, type: ClassMirror) {
    getField(owner.type.toAsmType(), name, type.type.toAsmType())
  }

  fun getField(owner: ClassMirror, name: String, type: Type) {
    getField(owner.type.toAsmType(), name, type)
  }

  fun putField(owner: ClassMirror, field: FieldMirror) {
    putField(owner.type.toAsmType(), field.name, field.type.toAsmType())
  }

  fun putField(owner: ClassMirror, name: String, type: ClassMirror) {
    putField(owner.type.toAsmType(), name, type.type.toAsmType())
  }

  fun putField(owner: ClassMirror, name: String, type: Type) {
    putField(owner.type.toAsmType(), name, type)
  }

  fun invokeStatic(owner: ClassMirror, method: Method) {
    invokeStatic(owner.type.toAsmType(), method)
  }

  fun invokeStatic(owner: ClassMirror, method: MethodMirror) {
    invokeStatic(owner.type.toAsmType(), Methods.get(method))
  }

  fun invokeVirtual(owner: ClassMirror, method: Method) {
    invokeVirtual(owner.type.toAsmType(), method)
  }

  fun invokeVirtual(owner: ClassMirror, method: MethodMirror) {
    invokeVirtual(owner.type.toAsmType(), Methods.get(method))
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
