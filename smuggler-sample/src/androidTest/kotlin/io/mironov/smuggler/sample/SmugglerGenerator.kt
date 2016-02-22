package io.mironov.smuggler.sample

import java.util.Random

class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  fun nextBoolean() = random.nextBoolean()
  fun nextInt() = random.nextInt()
  fun nextLong() = random.nextLong()
  fun nextFloat() = random.nextFloat()
  fun nextDouble() = random.nextDouble()
  fun nextShort() = random.nextInt().toShort()
  fun nextByte() = random.nextInt().toByte()
  fun nextChar() = random.nextInt().toChar()
  fun nextString() = String(nextCharArray())
  fun <E : Enum<E>> nextEnum(clazz: Class<E>) = clazz.enumConstants[random.nextInt(clazz.enumConstants.size)]

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
