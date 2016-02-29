package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.annotations.AnnotationDelegate
import org.objectweb.asm.Type
import java.io.Serializable
import java.util.HashSet

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
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

  val OBJECT = Type.getType(Any::class.java)
  val CLASS = Type.getType(Class::class.java)
  val CLASS_LOADER = Type.getType(ClassLoader::class.java)
  val ENUM = Type.getType(Enum::class.java)
  val SERIALIZABLE = Type.getType(Serializable::class.java)

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

  val BOXED_BYTE = Type.getType(java.lang.Byte::class.java)
  val BOXED_CHAR = Type.getType(java.lang.Character::class.java)
  val BOXED_DOUBLE = Type.getType(java.lang.Double::class.java)
  val BOXED_FLOAT = Type.getType(java.lang.Float::class.java)
  val BOXED_INT = Type.getType(java.lang.Integer::class.java)
  val BOXED_LONG = Type.getType(java.lang.Long::class.java)
  val BOXED_SHORT = Type.getType(java.lang.Short::class.java)
  val BOXED_BOOLEAN = Type.getType(java.lang.Boolean::class.java)

  val ANDROID_PARCEL = Type.getObjectType("android/os/Parcel")
  val ANDROID_PARCELABLE = Type.getObjectType("android/os/Parcelable")
  val ANDROID_CREATOR = Type.getObjectType("android/os/Parcelable\$Creator")
  val ANDROID_BUNDLE = Type.getObjectType("android/os/Bundle")
  val ANDROID_SPARSE_BOOLEAN_ARRAY = Type.getObjectType("android/util/SparseBooleanArray")
  val ANDROID_SPARSE_ARRAY = Type.getObjectType("android/util/SparseArray")

  val SMUGGLER_PARCELABLE = Type.getObjectType("io/mironov/smuggler/AutoParcelable")
  val SMUGGLER_FACTORY = Type.getObjectType("io/mironov/smuggler/SmugglerFactory")

  fun getArrayType(type: Type): Type {
    return Type.getType("[${type.descriptor}")
  }

  fun getElementType(type: Type): Type {
    if (type.sort != Type.ARRAY) {
      throw IllegalArgumentException("Types.getElementType() can only be called for array types")
    }

    val element = type.elementType
    val dimensions = type.dimensions

    return 0.until(dimensions - 1).fold(element) { value, index ->
      getArrayType(value)
    }
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

  fun isPrimitive(type: Type): Boolean {
    return type in Types.PRIMITIVE_TYPES
  }
}
