package com.joom.smuggler.sample

import android.util.SparseArray
import android.util.SparseBooleanArray
import com.joom.smuggler.AutoParcelable
import java.lang.reflect.Modifier
import java.util.Arrays
import java.util.HashSet

object SmugglerEquivalence {
  fun equals(left: Any?, right: Any?): Boolean = nullableEquals(left, right) { left, right ->
    val leftClass = left.javaClass
    val rightClass = right.javaClass

    if (leftClass.isSubclassOf<Map<*, *>>() && rightClass.isSubclassOf<Map<*, *>>()) {
      return equals(left as Map<*, *>, right as Map<*, *>)
    }

    if (leftClass.isSubclassOf<List<*>>() && rightClass.isSubclassOf<List<*>>()) {
      return equals(left as List<*>, right as List<*>)
    }

    if (leftClass.isSubclassOf<Set<*>>() && rightClass.isSubclassOf<Set<*>>()) {
      return equals(left as Set<*>, right as Set<*>)
    }

    if (leftClass != rightClass) {
      return false
    }

    if (leftClass.isArray && !rightClass.isArray) {
      return false
    }

    if (!leftClass.isArray && rightClass.isArray) {
      return false
    }

    if (leftClass.isArray && rightClass.isArray) {
      return when (leftClass) {
        BooleanArray::class.java -> Arrays.equals(left as BooleanArray, right as BooleanArray)
        ByteArray::class.java -> Arrays.equals(left as ByteArray, right as ByteArray)
        CharArray::class.java -> Arrays.equals(left as CharArray, right as CharArray)
        LongArray::class.java -> Arrays.equals(left as LongArray, right as LongArray)
        IntArray::class.java -> Arrays.equals(left as IntArray, right as IntArray)
        FloatArray::class.java -> Arrays.equals(left as FloatArray, right as FloatArray)
        DoubleArray::class.java -> Arrays.equals(left as DoubleArray, right as DoubleArray)
        ShortArray::class.java -> Arrays.equals(left as ShortArray, right as ShortArray)
        else -> equals(left as Array<*>, right as Array<*>)
      }
    }

    if (leftClass == SparseBooleanArray::class.java) {
      return equals(left as SparseBooleanArray, right as SparseBooleanArray)
    }

    if (leftClass == SparseArray::class.java) {
      return equals(left as SparseArray<*>, right as SparseArray<*>)
    }

    if (leftClass.isSubclassOf<AutoParcelable>()) {
      return equals(left as AutoParcelable, right as AutoParcelable)
    }

    return left == right
  }

  fun hashCode(value: Any?): Int = nullableHashCode(value) { value ->
    val clazz = value.javaClass

    if (clazz.isArray) {
      return when (value.javaClass) {
        BooleanArray::class.java -> Arrays.hashCode(value as BooleanArray)
        ByteArray::class.java -> Arrays.hashCode(value as ByteArray)
        CharArray::class.java -> Arrays.hashCode(value as CharArray)
        LongArray::class.java -> Arrays.hashCode(value as LongArray)
        IntArray::class.java -> Arrays.hashCode(value as IntArray)
        FloatArray::class.java -> Arrays.hashCode(value as FloatArray)
        DoubleArray::class.java -> Arrays.hashCode(value as DoubleArray)
        ShortArray::class.java -> Arrays.hashCode(value as ShortArray)
        else -> hashCode(value as Array<*>)
      }
    }

    if (clazz.isSubclassOf<Map<*, *>>()) {
      return hashCode(value as Map<*, *>)
    }

    if (clazz.isSubclassOf<List<*>>()) {
      return hashCode(value as List<*>)
    }

    if (clazz.isSubclassOf<Set<*>>()) {
      return hashCode(value as Set<*>)
    }

    if (clazz == SparseBooleanArray::class.java) {
      return hashCode(value as SparseBooleanArray)
    }

    if (clazz == SparseArray::class.java) {
      return hashCode(value as SparseArray<*>)
    }

    if (clazz.isSubclassOf<AutoParcelable>()) {
      return hashCode(value as AutoParcelable)
    }

    return value.hashCode()
  }

  fun equals(left: Array<*>?, right: Array<*>?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      equals(left[it], right[it])
    }
  }

  fun hashCode(value: Array<*>?): Int = nullableHashCode(value) { value ->
    value.fold(0) { hash, element ->
      31 * hash + hashCode(element)
    }
  }

  fun equals(left: List<*>?, right: List<*>?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      equals(left[it], right[it])
    }
  }

  fun hashCode(value: List<*>?): Int = nullableHashCode(value) { value ->
    value.fold(0) { hash, element ->
      31 * hash + hashCode(element)
    }
  }

  fun equals(left: Set<*>?, right: Set<*>?): Boolean = nullableEquals(left, right) { left, right ->
    left.mapTo(HashSet(), { Wrapper(it) }) == right.mapTo(HashSet(), { Wrapper(it) })
  }

  fun hashCode(value: Set<*>?): Int = nullableHashCode(value) { value ->
    value.mapTo(HashSet(), { Wrapper(it) }).hashCode()
  }

  fun equals(left: Map<*, *>?, right: Map<*, *>?): Boolean = nullableEquals(left, right) { left, right ->
    val one = left.entries.associateBy({ Wrapper(it.key) }, { Wrapper(it.value) })
    val two = right.entries.associateBy({ Wrapper(it.key) }, { Wrapper(it.value) })

    one == two
  }

  fun hashCode(value: Map<*, *>?): Int = nullableHashCode(value) { value ->
    value.entries.associateBy({ Wrapper(it.key) }, { Wrapper(it.value) }).hashCode()
  }

  fun equals(left: SparseBooleanArray?, right: SparseBooleanArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size() == right.size() && 0.until(left.size()).all {
      left.keyAt(it) == right.keyAt(it) && equals(left.valueAt(it), right.valueAt(it))
    }
  }

  fun hashCode(value: SparseBooleanArray?): Int = nullableHashCode(value) { value ->
    0.until(value.size()).fold(0) { hash, index ->
      31 * 31 * hash + 31 * hashCode(value.valueAt(index)) + value.keyAt(index)
    }
  }

  fun equals(left: SparseArray<*>, right: SparseArray<*>): Boolean = nullableEquals(left, right) { left, right ->
    left.size() == right.size() && 0.until(left.size()).all {
      left.keyAt(it) == right.keyAt(it) && equals(left.valueAt(it), right.valueAt(it))
    }
  }

  fun hashCode(value: SparseArray<*>?): Int = nullableHashCode(value) { value ->
    0.until(value.size()).fold(0) { hash, index ->
      31 * 31 * hash + 31 * hashCode(value.valueAt(index)) + value.keyAt(index)
    }
  }

  fun <P : AutoParcelable> equals(left: P?, right: P?): Boolean = nullableEquals(left, right) { left, right ->
    return left.javaClass.declaredFields
        .each { it.isAccessible = true }
        .filter { !Modifier.isStatic(it.modifiers) }
        .all { equals(it.get(left), it.get(right)) }
  }

  fun <P : AutoParcelable> hashCode(value: P?): Int = nullableHashCode(value) { value ->
    return value.javaClass.declaredFields
        .each { it.isAccessible = true }
        .filter { !Modifier.isStatic(it.modifiers) }
        .fold(0) { hash, field -> 31 * hash + hashCode(field.get(value)) }
  }

  inline fun <T> nullableEquals(left: T?, right: T?, equality: (T, T) -> Boolean): Boolean {
    if (left === right) {
      return true
    }

    if (left != null && right != null) {
      return equality(left, right)
    }

    return false
  }

  inline fun <T> nullableHashCode(value: T?, code: (T) -> Int): Int {
    return value?.let(code) ?: 0
  }

  private data class Wrapper<T>(private val value: T) {
    override fun equals(other: Any?): Boolean = other is Wrapper<*> && equals(value, other.value)
    override fun hashCode(): Int = hashCode(value)
  }

  private inline fun <reified T> Array<T>.each(action: (T) -> Unit): Array<T> = apply {
    forEach(action)
  }

  private inline fun <reified T : Any> Class<*>.isSubclassOf(): Boolean {
    return T::class.java.isAssignableFrom(this)
  }
}
