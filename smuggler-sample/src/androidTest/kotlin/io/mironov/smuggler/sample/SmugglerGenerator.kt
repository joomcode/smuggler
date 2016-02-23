package io.mironov.smuggler.sample

import java.util.Random

class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  inline fun <T : Any> nextNullable(factory: () -> T): T? = if (nextNullableProbability()) null else factory()

  fun nextArraySize() = random.nextInt(MAX_ARRAY_SIZE)
  fun nextNullableProbability() = random.nextInt(3) == 0

  fun nextBoolean() = random.nextBoolean()
  fun nextNullableBoolean() = nextNullable { nextBoolean() }

  fun nextInt() = random.nextInt()
  fun nextNullableInt() = nextNullable { nextInt() }

  fun nextLong() = random.nextLong()
  fun nextNullableLong() = nextNullable { nextLong() }

  fun nextFloat() = random.nextFloat()
  fun nextNullableFloat() = nextNullable { nextFloat() }

  fun nextDouble() = random.nextDouble()
  fun nextNullableDouble() = nextNullable { nextDouble() }

  fun nextShort() = random.nextInt().toShort()
  fun nextNullableShort() = nextNullable { nextShort() }

  fun nextByte() = random.nextInt().toByte()
  fun nextNullableByte() = nextNullable { nextByte() }

  fun nextChar() = random.nextInt().toChar()
  fun nextNullableChar() = nextNullable { nextChar() }

  fun nextString() = String(nextCharArray())
  fun nextNullableString() = nextNullable { nextString() }

  fun <E : Enum<E>> nextEnum(clazz: Class<E>) = clazz.enumConstants[random.nextInt(clazz.enumConstants.size)]
  fun <E : Enum<E>> nextNullableEnum(clazz: Class<E>) = nextNullable { nextEnum(clazz) }

  fun nextBooleanArray() = BooleanArray(nextArraySize()) { nextBoolean() }
  fun nextNullableBooleanArray() = nextNullable { BooleanArray(nextArraySize()) { nextBoolean() } }

  fun nextIntArray() = IntArray(nextArraySize()) { nextInt() }
  fun nextNullableIntArray() = nextNullable { IntArray(nextArraySize()) { nextInt() } }

  fun nextLongArray() = LongArray(nextArraySize()) { nextLong() }
  fun nextNullableLongArray() = nextNullable { LongArray(nextArraySize()) { nextLong() } }

  fun nextFloatArray() = FloatArray(nextArraySize()) { nextFloat() }
  fun nextNullableFloatArray() = nextNullable { FloatArray(nextArraySize()) { nextFloat() } }

  fun nextDoubleArray() = DoubleArray(nextArraySize()) { nextDouble() }
  fun nextNullableDoubleArray() = nextNullable { DoubleArray(nextArraySize()) { nextDouble() } }

  fun nextShortArray() = ShortArray(nextArraySize()) { nextShort() }
  fun nextNullableShortArray() = nextNullable { ShortArray(nextArraySize()) { nextShort() } }

  fun nextByteArray() = ByteArray(nextArraySize()) { nextByte() }
  fun nextNullableByteArray() = nextNullable { ByteArray(nextArraySize()) { nextByte() } }

  fun nextCharArray() = CharArray(nextArraySize()) { nextChar() }
  fun nextNullableCharArray() = nextNullable { CharArray(nextArraySize()) { nextChar() } }

  fun nextStringArray() = Array(nextArraySize()) { nextString() }
  fun nextNullableStringArray() = nextNullable { Array(nextArraySize()) { nextString() } }

  inline fun <reified T : Any> nextArray(factory: (Int) -> T) = Array(nextArraySize()) { factory(it) }
  inline fun <reified T : Any> nextNullableArray(factory: (Int) -> T) = nextNullable { Array(nextArraySize()) { factory(it) } }

  private companion object {
    private const val MAX_ARRAY_SIZE = 25
  }
}
