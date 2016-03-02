package io.mironov.smuggler.sample

import android.util.SparseArray
import android.util.SparseBooleanArray
import io.mironov.smuggler.AutoParcelable
import java.lang.reflect.Modifier

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
        BooleanArray::class.java -> equals(left as BooleanArray, right as BooleanArray)
        ByteArray::class.java -> equals(left as ByteArray, right as ByteArray)
        CharArray::class.java -> equals(left as CharArray, right as CharArray)
        LongArray::class.java -> equals(left as LongArray, right as LongArray)
        IntArray::class.java -> equals(left as IntArray, right as IntArray)
        FloatArray::class.java -> equals(left as FloatArray, right as FloatArray)
        DoubleArray::class.java -> equals(left as DoubleArray, right as DoubleArray)
        ShortArray::class.java -> equals(left as ShortArray, right as ShortArray)
        else -> equals(left as Array<*>, right as Array<*>)
      }
    }

    if (leftClass == SparseBooleanArray::class.java) {
      return equals(left as SparseBooleanArray, right as SparseBooleanArray)
    }

    if (leftClass == SparseArray::class.java) {
      return equals(left as SparseArray<*>, right as SparseArray<*>)
    }

    if (AutoParcelable::class.java.isAssignableFrom(leftClass)) {
      return leftClass.declaredFields
          .each { it.isAccessible = true }
          .filter { !Modifier.isStatic(it.modifiers) }
          .all { equals(it.get(left), it.get(right)) }
    }

    return left == right
  }

  fun equals(left: BooleanArray?, right: BooleanArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: ByteArray?, right: ByteArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: CharArray?, right: CharArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: LongArray?, right: LongArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: IntArray?, right: IntArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: FloatArray?, right: FloatArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: DoubleArray?, right: DoubleArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: ShortArray?, right: ShortArray?): Boolean = nullableEquals(left, right) { left, right ->
    left.size == right.size && 0.until(left.size).all {
      left[it] == right[it]
    }
  }

  fun equals(left: Array<*>?, right: Array<*>?): Boolean = nullableEquals(left, right) { left, right ->
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

  inline fun <T> nullableEquals(left: T?, right: T?, equality: (T, T) -> Boolean): Boolean {
    if (left != null && right != null) {
      return equality(left, right)
    }

    if (left == null && right == null) {
      return true
    }

    return false
  }

  private inline fun <T> Array<T>.each(action: (T) -> Unit): Array<T> = apply {
    forEach(action)
  }
}
