package io.mironov.smuggler.sample

import android.support.test.runner.AndroidJUnit4
import io.mironov.smuggler.AutoParcelable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Arrays

@RunWith(AndroidJUnit4::class)
class SmugglerTest {
  private lateinit var generator: SmugglerGenerator

  @Before fun setUp() {
    generator = SmugglerGenerator(9999)
  }

  @Test fun shouldWorkWithPrimitives() {
    data class Primitives(
        val boolean: Boolean,
        val byte: Byte,
        val char: Char,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double,
        val string: String
    ) : AutoParcelable

    times(100) {
      SmugglerAssertions.verify(Primitives(
          boolean = generator.nextBoolean(),
          byte = generator.nextByte(),
          char = generator.nextChar(),
          short = generator.nextShort(),
          int = generator.nextInt(),
          long = generator.nextLong(),
          float = generator.nextFloat(),
          double = generator.nextDouble(),
          string = generator.nextString()
      ))
    }
  }

  @Test fun shouldWorkWithNestedObjects() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val id: Long,
        val timestamp: Long,
        val text: String,
        val sender: User,
        val seen: Boolean
    ) : AutoParcelable

    times(100) {
      SmugglerAssertions.verify(Message(
          id = generator.nextLong(),
          timestamp = generator.nextLong(),
          text = generator.nextString(),
          seen = generator.nextBoolean(),
          sender = User(
              firstName = generator.nextString(),
              lastName = generator.nextString()
          )
      ))
    }
  }

  @Test fun shouldWorkWithPrimitiveArrays() {
    data class PrimitiveArrays(
        val booleans: BooleanArray,
        val bytes: ByteArray,
        val chars: CharArray,
        val ints: IntArray,
        val longs: LongArray,
        val floats: FloatArray,
        val doubles: DoubleArray,
        val strings: Array<String>
    ) : AutoParcelable {
      override fun equals(other: Any?): Boolean {
        if (other == null || other !is PrimitiveArrays) {
          return false
        }

        return Arrays.equals(booleans, other.booleans) &&
            Arrays.equals(bytes, other.bytes) &&
            Arrays.equals(chars, other.chars) &&
            Arrays.equals(ints, other.ints) &&
            Arrays.equals(longs, other.longs) &&
            Arrays.equals(floats, other.floats) &&
            Arrays.equals(doubles, other.doubles) &&
            Arrays.equals(strings, other.strings)
      }
    }

    times(100) {
      SmugglerAssertions.verify(PrimitiveArrays(
          booleans = generator.nextBooleanArray(),
          bytes = generator.nextByteArray(),
          chars = generator.nextCharArray(),
          ints = generator.nextIntArray(),
          longs = generator.nextLongArray(),
          floats = generator.nextFloatArray(),
          doubles = generator.nextDoubleArray(),
          strings = generator.nextStringArray()
      ))
    }
  }

  @Test fun shouldWorkWithEnums() {
    data class Enums(
        val magic: Magic,
        val foo: Foo
    ) : AutoParcelable

    times(100) {
      SmugglerAssertions.verify(Enums(
          magic = generator.nextEnum(Magic::class.java),
          foo = generator.nextEnum(Foo::class.java)
      ))
    }
  }

  @Test fun shouldWorkWithOptionalPrimitives() {
    data class Optionals(
        val boolean: Boolean?,
        val byte: Byte?,
        val char: Char?,
        val short: Short?,
        val int: Int?,
        val long: Long?,
        val float: Float?,
        val double: Double?,
        val string: String?,
        val magic: Magic?,
        val foo: Foo?
    ) : AutoParcelable

    times(100) {
      SmugglerAssertions.verify(Optionals(
          boolean = generator.nextNullableBoolean(),
          byte = generator.nextNullableByte(),
          char = generator.nextNullableChar(),
          short = generator.nextNullableShort(),
          int = generator.nextNullableInt(),
          long = generator.nextNullableLong(),
          float = generator.nextNullableFloat(),
          double = generator.nextNullableDouble(),
          string = generator.nextNullableString(),
          magic = generator.nextNullableEnum(Magic::class.java),
          foo = generator.nextNullableEnum(Foo::class.java)
      ))
    }
  }

  private enum class Magic {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
  }

  private enum class Foo {
    FOO, BAR, FOO_FOO, BAR_BAR, FOO_BAR, BAR_FOO
  }

  private inline fun times(count: Int, action: () -> Unit) {
    for (i in 0..count - 1) {
      action()
    }
  }
}
