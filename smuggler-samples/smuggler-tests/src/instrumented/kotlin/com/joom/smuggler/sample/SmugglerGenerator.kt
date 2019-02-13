package com.joom.smuggler.sample

import android.util.SparseArray
import android.util.SparseBooleanArray
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Random

@Suppress("NOTHING_TO_INLINE")
class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  inline fun <T : Any> nextValue(factory: () -> T): T? = factory()
  inline fun <T : Any> nextNullableValue(factory: () -> T): T? = if (nextNullableProbability()) null else factory()

  inline fun <reified T : Any> nextArray(factory: (Int) -> T) = Array(nextArraySize()) { factory(it) }
  inline fun <reified T : Any> nextNullableArray(factory: (Int) -> T) = nextNullableValue { nextArray(factory) }

  inline fun <reified T : Any> nextList(element: (Int) -> T): List<T> = createList({ ArrayList<T>() }, element)
  inline fun <reified T : Any> nextNullableList(element: (Int) -> T): List<T>? = nextNullableValue { nextList(element) }

  inline fun <reified T : Any> nextSet(element: (Int) -> T): Set<T> = createSet({ LinkedHashSet<T>() }, element)
  inline fun <reified T : Any> nextNullableSet(element: (Int) -> T): Set<T>? = nextNullableValue { nextSet(element) }

  inline fun <reified K : Any, reified V : Any> nextMap(key: (Int) -> K, value: (Int) -> V): Map<K, V> = createMap({ LinkedHashMap<K, V>() }, key, value)
  inline fun <reified K : Any, reified V : Any> nextNullableMap(key: (Int) -> K, value: (Int) -> V): Map<K, V>? = nextNullableValue { nextMap(key, value) }

  inline fun <reified L : MutableList<T>, reified T : Any> nextList(factory: (Int) -> L, element: (Int) -> T): L = createList(factory, element)
  inline fun <reified L : MutableList<T>, reified T : Any> nextNullableList(factory: (Int) -> L, element: (Int) -> T): L? = nextNullableValue { nextList(factory, element) }

  inline fun <reified M : MutableMap<K, V>, reified K : Any, reified V : Any> nextMap(factory: (Int) -> M, key: (Int) -> K, value: (Int) -> V): M = createMap(factory, key, value)
  inline fun <reified M : MutableMap<K, V>, reified K : Any, reified V : Any> nextNullableMap(factory: (Int) -> M, key: (Int) -> K, value: (Int) -> V): M? = nextNullableValue { nextMap(factory, key, value) }

  inline fun <reified S : MutableSet<T>, reified T : Any> nextSet(factory: (Int) -> S, element: (Int) -> T): S = createSet(factory, element)
  inline fun <reified S : MutableSet<T>, reified T : Any> nextNullableSet(factory: (Int) -> S, element: (Int) -> T): S? = nextNullableValue { nextSet(factory, element) }

  fun <T : Any> nextElement(array: Array<T>): T = array[random.nextInt(array.size)]
  fun <T : Any> nextNullableElement(array: Array<T>): T? = nextNullableValue { nextElement(array) }

  fun <T : Any> nextElement(list: List<T>): T = list[random.nextInt(list.size)]
  fun <T : Any> nextNullableElement(list: List<T>): T? = nextNullableValue { nextElement(list) }

  fun nextArraySize() = random.nextInt(MAX_ARRAY_SIZE)
  fun nextNullableProbability() = random.nextInt(3) == 0

  fun nextBoolean() = random.nextBoolean()
  fun nextNullableBoolean() = nextNullableValue { nextBoolean() }

  fun nextInt() = random.nextInt()
  fun nextNullableInt() = nextNullableValue { nextInt() }

  fun nextLong() = random.nextLong()
  fun nextNullableLong() = nextNullableValue { nextLong() }

  fun nextFloat() = random.nextFloat()
  fun nextNullableFloat() = nextNullableValue { nextFloat() }

  fun nextDouble() = random.nextDouble()
  fun nextNullableDouble() = nextNullableValue { nextDouble() }

  fun nextShort() = random.nextInt().toShort()
  fun nextNullableShort() = nextNullableValue { nextShort() }

  fun nextByte() = random.nextInt().toByte()
  fun nextNullableByte() = nextNullableValue { nextByte() }

  fun nextChar() = random.nextInt().toChar()
  fun nextNullableChar() = nextNullableValue { nextChar() }

  fun nextString() = String(nextCharArray())
  fun nextNullableString() = nextNullableValue { nextString() }

  fun <E : Enum<E>> nextEnum(clazz: Class<E>) = clazz.enumConstants[random.nextInt(clazz.enumConstants.size)]
  fun <E : Enum<E>> nextNullableEnum(clazz: Class<E>) = nextNullableValue { nextEnum(clazz) }

  fun nextBooleanArray() = BooleanArray(nextArraySize()) { nextBoolean() }
  fun nextNullableBooleanArray() = nextNullableValue { nextBooleanArray() }

  fun nextIntArray() = IntArray(nextArraySize()) { nextInt() }
  fun nextNullableIntArray() = nextNullableValue { nextIntArray() }

  fun nextLongArray() = LongArray(nextArraySize()) { nextLong() }
  fun nextNullableLongArray() = nextNullableValue { nextLongArray() }

  fun nextFloatArray() = FloatArray(nextArraySize()) { nextFloat() }
  fun nextNullableFloatArray() = nextNullableValue { nextFloatArray() }

  fun nextDoubleArray() = DoubleArray(nextArraySize()) { nextDouble() }
  fun nextNullableDoubleArray() = nextNullableValue { nextDoubleArray() }

  fun nextShortArray() = ShortArray(nextArraySize()) { nextShort() }
  fun nextNullableShortArray() = nextNullableValue { nextShortArray() }

  fun nextByteArray() = ByteArray(nextArraySize()) { nextByte() }
  fun nextNullableByteArray() = nextNullableValue { nextByteArray() }

  fun nextCharArray() = CharArray(nextArraySize()) { nextChar() }
  fun nextNullableCharArray() = nextNullableValue { nextCharArray() }

  fun nextStringArray() = Array(nextArraySize()) { nextString() }
  fun nextNullableStringArray() = nextNullableValue { nextStringArray() }

  fun nextSparseBooleanArray() = createSparseBooleanArray()
  fun nextNullableSparseBooleanArray() = nextNullableValue { nextSparseBooleanArray() }

  fun nextSparseBooleanArrayArray() = nextArray { nextSparseBooleanArray() }
  fun nextNullableSparseBooleanArrayArray() = nextNullableValue { nextSparseBooleanArrayArray() }

  inline fun <reified T> nextSparseArray(factory: (Int) -> T) = createSparseArray(factory)
  inline fun <reified T> nextNullableSparseArray(factory: (Int) -> T) = nextNullableValue { nextSparseArray(factory) }

  inline fun <reified T> nextSparseArrayArray(factory: (Int) -> T) = nextArray { createSparseArray(factory) }
  inline fun <reified T> nextNullableSparseArrayArray(factory: (Int) -> T) = nextNullableValue { nextSparseArrayArray(factory) }

  inline fun <reified L : MutableList<T>, reified T> createList(factory: (Int) -> L, element: (Int) -> T): L {
    return factory(0).apply {
      for (index in 0..nextArraySize() - 1) {
        add(element(index))
      }
    }
  }

  inline fun <reified S : MutableSet<T>, reified T> createSet(factory: (Int) -> S, element: (Int) -> T): S {
    return factory(0).apply {
      for (index in 0..nextArraySize() - 1) {
        add(element(index))
      }
    }
  }

  inline fun <reified M : MutableMap<K, V>, reified K, reified V> createMap(factory: (Int) -> M, key: (Int) -> K, value: (Int) -> V): M {
    return factory(0).apply {
      for (index in 0..nextArraySize() - 1) {
        put(key(index), value(index))
      }
    }
  }

  inline fun createSparseBooleanArray(): SparseBooleanArray {
    return SparseBooleanArray().apply {
      for (index in 0..nextArraySize() - 1) {
        put(nextInt(), nextBoolean())
      }
    }
  }

  inline fun <T> createSparseArray(factory: (Int) -> T): SparseArray<T> {
    return SparseArray<T>().apply {
      for (index in 0..nextArraySize() - 1) {
        put(nextInt(), factory(index))
      }
    }
  }

  private companion object {
    private const val MAX_ARRAY_SIZE = 25
  }
}
