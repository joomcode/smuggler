package io.mironov.smuggler.sample

import java.util.Random

class SmugglerGenerator(private val seed: Long) {
  private val random = Random(seed)

  fun nextBoolean(): Boolean {
    return random.nextBoolean()
  }

  fun nextInt(): Int {
    return random.nextInt()
  }

  fun nextLong(): Long {
    return random.nextLong()
  }

  fun nextFloat(): Float {
    return random.nextFloat()
  }

  fun nextDouble(): Double {
    return random.nextDouble()
  }

  fun nextShort(): Short {
    return random.nextInt().toShort()
  }

  fun nextByte(): Byte {
    return random.nextInt().toByte()
  }

  fun nextChar(): Char {
    return random.nextInt().toChar()
  }

  fun nextString(): String {
    val length = random.nextInt(25)
    val builder = StringBuilder(length)

    for (i in 0..length - 1) {
      builder.append(nextChar())
    }

    String

    return builder.toString()
  }

  fun nextBooleanArray(): BooleanArray {
    return BooleanArray(random.nextInt(25)) { nextBoolean() }
  }

  fun nextIntArray(): IntArray {
    return IntArray(random.nextInt(25)) { nextInt() }
  }

  fun nextLongArray(): LongArray {
    return LongArray(random.nextInt(25)) { nextLong() }
  }

  fun nextFloatArray(): FloatArray {
    return FloatArray(random.nextInt(25)) { nextFloat() }
  }

  fun nextDoubleArray(): DoubleArray {
    return DoubleArray(random.nextInt(25)) { nextDouble() }
  }

  fun nextShortArray(): ShortArray {
    return ShortArray(random.nextInt(25)) { nextShort() }
  }

  fun nextByteArray(): ByteArray {
    return ByteArray(random.nextInt(25)) { nextByte() }
  }

  fun nextCharArray(): CharArray {
    return CharArray(random.nextInt(25)) { nextChar() }
  }
}
