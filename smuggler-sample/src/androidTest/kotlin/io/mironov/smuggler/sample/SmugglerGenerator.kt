package io.mironov.smuggler.sample

import java.util.Random

class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  fun <T : Any> nextNullable(factory: () -> T): T? = if (random.nextInt(3) == 0) null else factory()

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

  fun nextBooleanArray() = BooleanArray(random.nextInt(MAX_ARRAY_SIZE)) { nextBoolean() }
  fun nextNullableBooleanArray() = nextNullable { BooleanArray(random.nextInt(MAX_ARRAY_SIZE)) { nextBoolean() } }

  fun nextIntArray() = IntArray(random.nextInt(MAX_ARRAY_SIZE)) { nextInt() }
  fun nextNullableIntArray() = nextNullable { IntArray(random.nextInt(MAX_ARRAY_SIZE)) { nextInt() } }

  fun nextLongArray() = LongArray(random.nextInt(MAX_ARRAY_SIZE)) { nextLong() }
  fun nextNullableLongArray() = nextNullable { LongArray(random.nextInt(MAX_ARRAY_SIZE)) { nextLong() } }

  fun nextFloatArray() = FloatArray(random.nextInt(MAX_ARRAY_SIZE)) { nextFloat() }
  fun nextNullableFloatArray() = nextNullable { FloatArray(random.nextInt(MAX_ARRAY_SIZE)) { nextFloat() } }

  fun nextDoubleArray() = DoubleArray(random.nextInt(MAX_ARRAY_SIZE)) { nextDouble() }
  fun nextNullableDoubleArray() = nextNullable { DoubleArray(random.nextInt(MAX_ARRAY_SIZE)) { nextDouble() } }

  fun nextShortArray() = ShortArray(random.nextInt(MAX_ARRAY_SIZE)) { nextShort() }
  fun nextNullableShortArray() = nextNullable { ShortArray(random.nextInt(MAX_ARRAY_SIZE)) { nextShort() } }

  fun nextByteArray() = ByteArray(random.nextInt(MAX_ARRAY_SIZE)) { nextByte() }
  fun nextNullableByteArray() = nextNullable { ByteArray(random.nextInt(MAX_ARRAY_SIZE)) { nextByte() } }

  fun nextCharArray() = CharArray(random.nextInt(MAX_ARRAY_SIZE)) { nextChar() }
  fun nextNullableCharArray() = nextNullable { CharArray(random.nextInt(MAX_ARRAY_SIZE)) { nextChar() } }

  fun nextStringArray() = Array(random.nextInt(MAX_ARRAY_SIZE)) { nextString() }
  fun nextNullableStringArray() = nextNullable { Array(random.nextInt(MAX_ARRAY_SIZE)) { nextString() } }

  private companion object {
    private const val MAX_ARRAY_SIZE = 25
  }
}
