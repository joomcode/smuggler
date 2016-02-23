package io.mironov.smuggler.sample

import java.util.Random

class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  fun <T : Any> nextNullable(factory: () -> T): T? = if (random.nextInt(3) == 0) null else factory()

  fun nextBoolean(): Boolean = random.nextBoolean()
  fun nextNullableBoolean(): Boolean? = nextNullable { nextBoolean() }

  fun nextInt(): Int = random.nextInt()
  fun nextNullableInt(): Int? = nextNullable { nextInt() }

  fun nextLong(): Long = random.nextLong()
  fun nextNullableLong(): Long? = nextNullable { nextLong() }

  fun nextFloat(): Float = random.nextFloat()
  fun nextNullableFloat(): Float? = nextNullable { nextFloat() }

  fun nextDouble(): Double = random.nextDouble()
  fun nextNullableDouble(): Double? = nextNullable { nextDouble() }

  fun nextShort(): Short = random.nextInt().toShort()
  fun nextNullableShort(): Short? = nextNullable { nextShort() }

  fun nextByte(): Byte = random.nextInt().toByte()
  fun nextNullableByte(): Byte? = nextNullable { nextByte() }

  fun nextChar(): Char = random.nextInt().toChar()
  fun nextNullableChar(): Char? = nextNullable { nextChar() }

  fun nextString(): String = String(nextCharArray())
  fun nextNullableString(): String? = nextNullable { nextString() }

  fun <E : Enum<E>> nextEnum(clazz: Class<E>): E = clazz.enumConstants[random.nextInt(clazz.enumConstants.size)]
  fun <E : Enum<E>> nextNullableEnum(clazz: Class<E>): E? = nextNullable { nextEnum(clazz) }

  fun nextBooleanArray() = BooleanArray(random.nextInt(MAX_ARRAY_SIZE)) { nextBoolean() }
  fun nextIntArray() = IntArray(random.nextInt(MAX_ARRAY_SIZE)) { nextInt() }
  fun nextLongArray() = LongArray(random.nextInt(MAX_ARRAY_SIZE)) { nextLong() }
  fun nextFloatArray() = FloatArray(random.nextInt(MAX_ARRAY_SIZE)) { nextFloat() }
  fun nextDoubleArray() = DoubleArray(random.nextInt(MAX_ARRAY_SIZE)) { nextDouble() }
  fun nextShortArray() = ShortArray(random.nextInt(MAX_ARRAY_SIZE)) { nextShort() }
  fun nextByteArray() = ByteArray(random.nextInt(MAX_ARRAY_SIZE)) { nextByte() }
  fun nextCharArray() =  CharArray(random.nextInt(MAX_ARRAY_SIZE)) { nextChar() }
  fun nextStringArray() = Array(random.nextInt(MAX_ARRAY_SIZE)) { nextString() }

  private companion object {
    private const val MAX_ARRAY_SIZE = 25
  }
}
