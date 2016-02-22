package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.annotations.AnnotationDelegate
import org.objectweb.asm.Type
import java.util.HashMap
import java.util.HashSet
import java.util.IdentityHashMap

internal object Types {
  private val PRIMITIVE_TYPES = HashSet<Type>().apply {
    add(Type.BYTE_TYPE)
    add(Type.CHAR_TYPE)
    add(Type.DOUBLE_TYPE)
    add(Type.FLOAT_TYPE)
    add(Type.INT_TYPE)
    add(Type.LONG_TYPE)
    add(Type.SHORT_TYPE)
    add(Type.BOOLEAN_TYPE)
    add(Type.VOID_TYPE)
  }

  private val PRIMITIVES = HashMap<String, String>().apply {
    put("byte", "B")
    put("char", "C")
    put("double", "D")
    put("float", "F")
    put("int", "I")
    put("long", "J")
    put("short", "S")
    put("boolean", "Z")
    put("void", "V")
  }

  val OBJECT = Type.getType(Any::class.java)
  val CLASS = Type.getType(Class::class.java)
  val CLASS_LOADER = Type.getType(ClassLoader::class.java)
  val ENUM = Type.getType(Enum::class.java)

  val BYTE = Type.BYTE_TYPE
  val CHAR = Type.CHAR_TYPE
  val DOUBLE = Type.DOUBLE_TYPE
  val FLOAT = Type.FLOAT_TYPE
  val INT = Type.INT_TYPE
  val LONG = Type.LONG_TYPE
  val SHORT = Type.SHORT_TYPE
  val BOOLEAN = Type.BOOLEAN_TYPE
  val STRING = Type.getType(String::class.java)
  val VOID = Type.VOID_TYPE

  val MAP = Type.getType(Map::class.java)
  val IDENTITY_MAP = Type.getType(IdentityHashMap::class.java)

  val ANDROID_PARCEL = Type.getObjectType("android/os/Parcel")
  val ANDROID_PARCELABLE = Type.getObjectType("android/os/Parcelable")
  val ANDROID_CREATOR = Type.getObjectType("android/os/Parcelable\$Creator")
  val ANDROID_BUNDLE = Type.getObjectType("android/os/Bundle")

  val SMUGGLER_PARCELABLE = Type.getObjectType("io/mironov/smuggler/AutoParcelable")
  val SMUGGLER_FACTORY = Type.getObjectType("io/mironov/smuggler/SmugglerFactory")

  fun getClassType(name: String): Type {
    return if (!name.endsWith("[]")) {
      Type.getType("${Types.PRIMITIVES[name.replace('.', '/')] ?: "L${name.replace('.', '/')};"}")
    } else {
      Types.getArrayType(getClassType(name.substring(0, name.length - 2)))
    }
  }

  fun getArrayType(type: Type): Type {
    return Type.getType("[${type.descriptor}")
  }

  fun getAnnotationType(clazz: Class<*>): Type {
    return clazz.getAnnotation(AnnotationDelegate::class.java)?.run {
      Type.getObjectType(value.replace('.', '/'))
    } ?: Type.getType(clazz)
  }

  fun getGeneratedType(type: Type, name: String): Type {
    return Type.getObjectType("${type.internalName}\$\$$name")
  }

  fun getClassFilePath(type: Type): String {
    return "${type.internalName}.class"
  }

  fun isSystemClass(type: Type): Boolean {
    val name = type.className

    if (name.endsWith(".Nullable")) {
      return false
    }

    if (name.endsWith(".NotNull")) {
      return false
    }

    if (name == "kotlin.Metadata") {
      return false
    }

    return arrayOf("android.", "java.", "kotlin.", "dalvik.").any {
      name.startsWith(it)
    }
  }

  fun isPrimitive(type: Type): Boolean {
    return type in Types.PRIMITIVE_TYPES
  }
}
