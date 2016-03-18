package io.mironov.smuggler.compiler.common

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import org.objectweb.asm.Opcodes

internal val ClassMirror.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val ClassMirror.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val ClassMirror.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val ClassMirror.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val ClassMirror.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val ClassMirror.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val ClassMirror.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val ClassMirror.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val MethodMirror.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val MethodMirror.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val MethodMirror.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val MethodMirror.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val MethodMirror.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val MethodMirror.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val MethodMirror.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val MethodMirror.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val FieldMirror.isPublic: Boolean
  get() = access and Opcodes.ACC_PUBLIC != 0

internal val FieldMirror.isPrivate: Boolean
  get() = access and Opcodes.ACC_PRIVATE != 0

internal val FieldMirror.isProtected: Boolean
  get() = access and Opcodes.ACC_PROTECTED != 0

internal val FieldMirror.isInterface: Boolean
  get() = access and Opcodes.ACC_INTERFACE != 0

internal val FieldMirror.isAnnotation: Boolean
  get() = access and Opcodes.ACC_ANNOTATION != 0

internal val FieldMirror.isAbstract: Boolean
  get() = access and Opcodes.ACC_ABSTRACT != 0

internal val FieldMirror.isSynthetic: Boolean
  get() = access and Opcodes.ACC_SYNTHETIC != 0

internal val FieldMirror.isStatic: Boolean
  get() = access and Opcodes.ACC_STATIC != 0


internal val Int.isPublic: Boolean
  get() = this and Opcodes.ACC_PUBLIC != 0

internal val Int.isPrivate: Boolean
  get() = this and Opcodes.ACC_PRIVATE != 0

internal val Int.isProtected: Boolean
  get() = this and Opcodes.ACC_PROTECTED != 0

internal val Int.isInterface: Boolean
  get() = this and Opcodes.ACC_INTERFACE != 0

internal val Int.isAnnotation: Boolean
  get() = this and Opcodes.ACC_ANNOTATION != 0

internal val Int.isAbstract: Boolean
  get() = this and Opcodes.ACC_ABSTRACT != 0

internal val Int.isSynthetic: Boolean
  get() = this and Opcodes.ACC_SYNTHETIC != 0

internal val Int.isStatic: Boolean
  get() = this and Opcodes.ACC_STATIC != 0
