package com.joom.smuggler.compiler.common

import com.joom.smuggler.compiler.annotations.AnnotationDelegate
import org.objectweb.asm.Type
import java.io.Serializable
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.LinkedList
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

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
  val CHAR_SEQUENCE = getType<CharSequence>()

  val ITERATOR = getType<Iterator<*>>()
  val COLLECTION = getType<Collection<*>>()
  val ENTRY = getType<Map.Entry<*, *>>()

  val LIST = getType<List<*>>()
  val ARRAY_LIST = getType<ArrayList<*>>()
  val LINKED_LIST = getType<LinkedList<*>>()

  val SET = getType<Set<*>>()
  val HASH_SET = getType<HashSet<*>>()
  val LINKED_SET = getType<LinkedHashSet<*>>()
  val SORTED_SET = getType<SortedSet<*>>()
  val TREE_SET = getType<TreeSet<*>>()

  val MAP = getType<Map<*, *>>()
  val HASH_MAP = getType<HashMap<*, *>>()
  val LINKED_MAP = getType<LinkedHashMap<*, *>>()
  val SORTED_MAP = getType<SortedMap<*, *>>()
  val TREE_MAP = getType<TreeMap<*, *>>()

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

  val BOXED_BYTE = getType<Byte>()
  val BOXED_CHAR = getType<Character>()
  val BOXED_DOUBLE = getType<Double>()
  val BOXED_FLOAT = getType<Float>()
  val BOXED_INT = getType<Integer>()
  val BOXED_LONG = getType<Long>()
  val BOXED_SHORT = getType<Short>()
  val BOXED_BOOLEAN = getType<java.lang.Boolean>()

  val ANDROID_PARCEL = Type.getObjectType("android/os/Parcel")
  val ANDROID_PARCELABLE = Type.getObjectType("android/os/Parcelable")
  val ANDROID_CREATOR = Type.getObjectType("android/os/Parcelable\$Creator")
  val ANDROID_BUNDLE = Type.getObjectType("android/os/Bundle")
  val ANDROID_SPARSE_BOOLEAN_ARRAY = Type.getObjectType("android/util/SparseBooleanArray")
  val ANDROID_SPARSE_ARRAY = Type.getObjectType("android/util/SparseArray")
  val ANDROID_TEXT_UTILS = Type.getObjectType("android/text/TextUtils")
  val ANDROID_LOG = Type.getObjectType("android/util/Log")

  val SMUGGLER_PARCELABLE = Type.getObjectType("com/joom/smuggler/AutoParcelable")
  val SMUGGLER_GLOBAL_ADAPTER = Type.getObjectType("com/joom/smuggler/GlobalAdapter")
  val SMUGGLER_LOCAL_ADAPTER = Type.getObjectType("com/joom/smuggler/LocalAdapter")
  val SMUGGLER_ADAPTER = Type.getObjectType("com/joom/smuggler/TypeAdapter")

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
    return type in PRIMITIVE_TYPES
  }
}
