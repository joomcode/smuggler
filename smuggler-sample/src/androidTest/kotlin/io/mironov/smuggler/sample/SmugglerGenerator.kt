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
  fun nextNullableBooleanArray() = nextNullable { nextBooleanArray() }

  fun nextIntArray() = IntArray(nextArraySize()) { nextInt() }
  fun nextNullableIntArray() = nextNullable { nextIntArray() }

  fun nextLongArray() = LongArray(nextArraySize()) { nextLong() }
  fun nextNullableLongArray() = nextNullable { nextLongArray() }

  fun nextFloatArray() = FloatArray(nextArraySize()) { nextFloat() }
  fun nextNullableFloatArray() = nextNullable { nextFloatArray() }

  fun nextDoubleArray() = DoubleArray(nextArraySize()) { nextDouble() }
  fun nextNullableDoubleArray() = nextNullable { nextDoubleArray() }

  fun nextShortArray() = ShortArray(nextArraySize()) { nextShort() }
  fun nextNullableShortArray() = nextNullable { nextShortArray() }

  fun nextByteArray() = ByteArray(nextArraySize()) { nextByte() }
  fun nextNullableByteArray() = nextNullable { nextByteArray() }

  fun nextCharArray() = CharArray(nextArraySize()) { nextChar() }
  fun nextNullableCharArray() = nextNullable { nextCharArray() }

  fun nextStringArray() = Array(nextArraySize()) { nextString() }
  fun nextNullableStringArray() = nextNullable { nextStringArray() }

  inline fun <reified T : Any> nextArray(factory: (Int) -> T) = Array(nextArraySize()) { factory(it) }
  inline fun <reified T : Any> nextNullableArray(factory: (Int) -> T) = nextNullable { nextArray(factory) }

  private companion object {
    private const val MAX_ARRAY_SIZE = 25
  }
}
