package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.reflect.ClassReference
import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.FieldSpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import org.objectweb.asm.Opcodes

internal val ClassReference.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val ClassReference.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val ClassReference.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val ClassReference.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val ClassReference.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val ClassReference.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val ClassReference.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val ClassReference.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val ClassSpec.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val ClassSpec.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val ClassSpec.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val ClassSpec.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val ClassSpec.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val ClassSpec.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val ClassSpec.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val ClassSpec.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val MethodSpec.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val MethodSpec.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val MethodSpec.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val MethodSpec.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val MethodSpec.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val MethodSpec.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val MethodSpec.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val MethodSpec.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val FieldSpec.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val FieldSpec.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val FieldSpec.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val FieldSpec.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val FieldSpec.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val FieldSpec.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val FieldSpec.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val FieldSpec.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0
