package io.mironov.smuggler.sample

import android.util.SparseArray
import android.util.SparseBooleanArray

object SmugglerEquivalence {
  fun equals(left: SparseBooleanArray, right: SparseBooleanArray): Boolean {
    if (left.size() != right.size()) {
      return false
    }

    for (i in 0..left.size() - 1) {
      if (left.keyAt(i) != right.keyAt(i) || left.valueAt(i) != right.valueAt(i)) {
        return false
      }
    }

    return true
  }

  fun equals(left: SparseArray<*>, right: SparseArray<*>): Boolean {
    if (left.size() != right.size()) {
      return false
    }

    for (i in 0..left.size() - 1) {
      if (left.keyAt(i) != right.keyAt(i) || left.valueAt(i) != right.valueAt(i)) {
        return false
      }
    }

    return true
  }
}
