package com.joom.smuggler.compiler.common

import org.objectweb.asm.Opcodes

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

internal val Int.isFinal: Boolean
  get() = this and Opcodes.ACC_FINAL != 0
