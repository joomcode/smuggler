package com.joom.smuggler.compiler.common

import com.joom.smuggler.compiler.SmugglerException
import com.joom.smuggler.compiler.annotations.AnnotationProxy
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.and
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.isAbstract
import io.michaelrocks.grip.isInterface
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.isStatic
import io.michaelrocks.grip.mirrors.isStaticInitializer
import io.michaelrocks.grip.mirrors.toAsmType
import io.michaelrocks.grip.not
import org.objectweb.asm.Type
import java.util.Arrays
import io.michaelrocks.grip.mirrors.Type as GripType

internal fun Grip.isSubclassOf(type: Type, parent: Type): Boolean {
  if (type.sort == Type.METHOD) {
    throw SmugglerException("Invalid argument type = $type. Types with ''sort'' == Type.METHOD are not allowed")
  }

  if (parent.sort == Type.METHOD) {
    throw SmugglerException("Invalid argument parent = $parent. Types with ''sort'' == Type.METHOD are not allowed")
  }

  if (type == parent) {
    return true
  }

  if (Types.isPrimitive(type) || Types.isPrimitive(parent)) {
    return type == parent
  }

  if (type == Types.OBJECT) {
    return parent == Types.OBJECT
  }

  if (parent == Types.OBJECT) {
    return true
  }

  if (type.sort == Type.ARRAY && parent.sort == Type.ARRAY) {
    return isSubclassOf(type.elementType, parent.elementType)
  }

  if (type.sort == Type.ARRAY || parent.sort == Type.ARRAY) {
    return false
  }

  val mirror = classRegistry.getClassMirror(GripType.Object(type))

  mirror.interfaces.forEach {
    if (isSubclassOf(it.toAsmType(), parent)) {
      return true
    }
  }

  return isSubclassOf(mirror.superType?.toAsmType() ?: return false, parent)
}

internal fun isSubclass(type: Type): (Grip, ClassMirror) -> Boolean = { grip, mirror ->
  grip.isSubclassOf(mirror.type.toAsmType(), type)
}

internal fun isTypeAdapter(): (Grip, ClassMirror) -> Boolean {
  return isSubclass(Types.SMUGGLER_ADAPTER)
}

internal fun isGlobalTypeAdapter(): (Grip, ClassMirror) -> Boolean {
  return isTypeAdapter() and annotatedWith(GripType.Object(Types.SMUGGLER_GLOBAL_ADAPTER))
}

internal fun isAutoParcelable(): (Grip, ClassMirror) -> Boolean {
  return not(isInterface()) and not(isAbstract()) and isSubclass(Types.SMUGGLER_PARCELABLE)
}

internal inline fun <reified A : Any> ClassMirror.getAnnotation(): A? {
  val annotation = annotations.firstOrNull {
    it.type.toAsmType() == Types.getAnnotationType(A::class.java)
  } ?: return null

  return AnnotationProxy.create(A::class.java, annotation)
}

internal fun ClassMirror.getStaticInitializer(): MethodMirror? {
  return methods.firstOrNull { it.isStaticInitializer }
}

internal fun ClassMirror.getDeclaredConstructor(vararg args: Type): MethodMirror? {
  return constructors.firstOrNull {
    !it.isStatic && Arrays.equals(it.type.toAsmType().argumentTypes, args)
  }
}

internal fun ClassMirror.getDeclaredMethod(name: String): MethodMirror? {
  return methods.singleOrNull { it.name == name }
}

internal fun ClassMirror.getDeclaredMethod(name: String, returns: Type, vararg args: Type): MethodMirror? {
  return methods.singleOrNull {
    it.name == name && it.type.returnType == returns && Arrays.equals(it.type.toAsmType().argumentTypes, args)
  }
}

internal fun ClassMirror.getDeclaredField(name: String): FieldMirror? {
  return fields.singleOrNull {
    it.name == name
  }
}
