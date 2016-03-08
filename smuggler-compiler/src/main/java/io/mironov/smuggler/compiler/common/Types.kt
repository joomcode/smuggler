package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.annotations.AnnotationDelegate
import org.objectweb.asm.Type
import java.io.Serializable
import java.util.Date
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

  val OBJECT = getType<Any>()
  val CLASS = getType<Class<*>>()
  val CLASS_LOADER = getType<ClassLoader>()
  val ENUM = getType<Enum<*>>()
  val SERIALIZABLE = getType<Serializable>()
  val DATE = getType<Date>()

  val ITERATOR = getType<java.util.Iterator<*>>()
  val COLLECTION = getType<java.util.Collection<*>>()
  val ENTRY = getType<java.util.Map.Entry<*, *>>()

  val LIST = getType<java.util.List<*>>()
  val ARRAY_LIST = getType<java.util.ArrayList<*>>()
  val LINKED_LIST = getType<java.util.LinkedList<*>>()

  val SET = getType<java.util.Set<*>>()
  val HASH_SET = getType<java.util.HashSet<*>>()
  val LINKED_SET = getType<java.util.LinkedHashSet<*>>()
  val SORTED_SET = getType<java.util.SortedSet<*>>()
  val TREE_SET = getType<java.util.TreeSet<*>>()

  val MAP = getType<java.util.Map<*, *>>()
  val HASH_MAP = getType<java.util.HashMap<*, *>>()
  val LINKED_MAP = getType<java.util.LinkedHashMap<*, *>>()
  val SORTED_MAP = getType<java.util.SortedMap<*, *>>()
  val TREE_MAP = getType<java.util.TreeMap<*, *>>()

  val BYTE = Type.BYTE_TYPE
  val CHAR = Type.CHAR_TYPE
  val DOUBLE = Type.DOUBLE_TYPE
  val FLOAT = Type.FLOAT_TYPE
  val INT = Type.INT_TYPE
  val LONG = Type.LONG_TYPE
  val SHORT = Type.SHORT_TYPE
  val BOOLEAN = Type.BOOLEAN_TYPE
  val STRING = getType<String>()
  val VOID = Type.VOID_TYPE

  val BOXED_BYTE = getType<java.lang.Byte>()
  val BOXED_CHAR = getType<java.lang.Character>()
  val BOXED_DOUBLE = getType<java.lang.Double>()
  val BOXED_FLOAT = getType<java.lang.Float>()
  val BOXED_INT = getType<java.lang.Integer>()
  val BOXED_LONG = getType<java.lang.Long>()
  val BOXED_SHORT = getType<java.lang.Short>()
  val BOXED_BOOLEAN = getType<java.lang.Boolean>()

  val ANDROID_PARCEL = Type.getObjectType("android/os/Parcel")
  val ANDROID_PARCELABLE = Type.getObjectType("android/os/Parcelable")
  val ANDROID_CREATOR = Type.getObjectType("android/os/Parcelable\$Creator")
  val ANDROID_BUNDLE = Type.getObjectType("android/os/Bundle")
  val ANDROID_SPARSE_BOOLEAN_ARRAY = Type.getObjectType("android/util/SparseBooleanArray")
  val ANDROID_SPARSE_ARRAY = Type.getObjectType("android/util/SparseArray")

  val SMUGGLER_PARCELABLE = Type.getObjectType("io/mironov/smuggler/AutoParcelable")
  val SMUGGLER_ADAPTER = Type.getObjectType("io/mironov/smuggler/TypeAdapter")

  inline fun <reified T : Any> getType(): Type {
    return Type.getType(T::class.java)
  }

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
