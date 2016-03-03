package io.mironov.smuggler.sample

import android.util.SparseArray
import android.util.SparseBooleanArray
import io.mironov.smuggler.AutoParcelable
import java.lang.reflect.Modifier
import java.util.Arrays

object SmugglerEquivalence {
  fun equals(left: Any?, right: Any?): Boolean = nullableEquals(left, right) { left, right ->
    val leftClass = left.javaClass
    val rightClass = right.javaClass

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

    if (List::class.java.isAssignableFrom(leftClass)) {
      return equals(left as List<*>, right as List<*>)
    }

    if (leftClass == SparseBooleanArray::class.java) {
      return equals(left as SparseBooleanArray, right as SparseBooleanArray)
    }

    if (leftClass == SparseArray::class.java) {
      return equals(left as SparseArray<*>, right as SparseArray<*>)
    }

    if (AutoParcelable::class.java.isAssignableFrom(leftClass)) {
      return equals(left as AutoParcelable, right as AutoParcelable)
    }

    return left == right
  }

  fun equals(left: Array<*>?, right: Array<*>?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      equals(left[it], right[it])
    }
  }

  fun equals(left: List<*>?, right: List<*>?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      equals(left[it], right[it])
    }
  }

  fun equals(left: SparseBooleanArray?, right: SparseBooleanArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size() == right.size() && 0.until(left.size()).all {
      left.keyAt(it) == right.keyAt(it) && equals(left.valueAt(it), right.valueAt(it))
    }
  }

  fun equals(left: SparseArray<*>, right: SparseArray<*>): Boolean = nullableEquals(left, right) { left, right ->
    left.size() == right.size() && 0.until(left.size()).all {
      left.keyAt(it) == right.keyAt(it) && equals(left.valueAt(it), right.valueAt(it))
    }
  }

  fun <P : AutoParcelable> equals(left: P?, right: P?): Boolean = nullableEquals(left, right) { left, right ->
    return left.javaClass.declaredFields
        .each { it.isAccessible = true }
        .filter { !Modifier.isStatic(it.modifiers) }
        .all { equals(it.get(left), it.get(right)) }
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

  private inline fun <T> Array<T>.each(action: (T) -> Unit): Array<T> = apply {
    forEach(action)
  }
}
