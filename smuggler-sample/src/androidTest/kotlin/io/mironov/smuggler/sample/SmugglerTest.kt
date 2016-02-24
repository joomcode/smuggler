package io.mironov.smuggler.sample

import android.support.test.runner.AndroidJUnit4
import android.text.TextUtils
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

    SmugglerAssertions.verify<Primitives> {
      Primitives(
          boolean = generator.nextBoolean(),
          byte = generator.nextByte(),
          char = generator.nextChar(),
          short = generator.nextShort(),
          int = generator.nextInt(),
          long = generator.nextLong(),
          float = generator.nextFloat(),
          double = generator.nextDouble(),
          string = generator.nextString()
      )
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

    SmugglerAssertions.verify<Message> {
      Message(
          id = generator.nextLong(),
          timestamp = generator.nextLong(),
          text = generator.nextString(),
          seen = generator.nextBoolean(),
          sender = User(
              firstName = generator.nextString(),
              lastName = generator.nextString()
          )
      )
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
        val shorts: ShortArray,
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
            Arrays.equals(shorts, other.shorts) &&
            Arrays.equals(strings, other.strings)
      }
    }

    SmugglerAssertions.verify<PrimitiveArrays> {
      PrimitiveArrays(
          booleans = generator.nextBooleanArray(),
          bytes = generator.nextByteArray(),
          chars = generator.nextCharArray(),
          ints = generator.nextIntArray(),
          longs = generator.nextLongArray(),
          floats = generator.nextFloatArray(),
          doubles = generator.nextDoubleArray(),
          shorts = generator.nextShortArray(),
          strings = generator.nextStringArray()
      )
    }
  }

  @Test fun shouldWorkWithEnums() {
    data class Enums(
        val magic: Magic,
        val foo: Foo
    ) : AutoParcelable

    SmugglerAssertions.verify<Enums> {
      Enums(
          magic = generator.nextEnum(Magic::class.java),
          foo = generator.nextEnum(Foo::class.java)
      )
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

    SmugglerAssertions.verify<Optionals> {
      Optionals(
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
      )
    }
  }

  @Test fun shouldWorkWithOptionalNestedObjects() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Payload(
        val text: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Status(
        val seen: Boolean,
        val delivered: Boolean
    ) : AutoParcelable

    data class Message(
        val user: User?,
        val payload: Payload?,
        val status: Status?
    ) : AutoParcelable

    SmugglerAssertions.verify<Message> {
      Message(
          user = generator.nextNullable {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          payload = generator.nextNullable {
            Payload(
                text = generator.nextString(),
                timestamp = generator.nextLong()
            )
          },
          status = generator.nextNullable {
            Status(
                seen = generator.nextBoolean(),
                delivered = generator.nextBoolean()
            )
          }
      )
    }
  }

  @Test fun shouldWorkWithCustomStaticClassInitializer() {
    SmugglerAssertions.consume(WithStaticClassInitializer.Companion)
    SmugglerAssertions.consume(WithStaticClassInitializer.EXTRA_PAYLOAD)
    SmugglerAssertions.consume(WithStaticClassInitializer.EXTRA_MESSAGE)

    SmugglerAssertions.verify(WithStaticClassInitializer::class.java)
    SmugglerAssertions.verify("payload", WithStaticClassInitializer.EXTRA_PAYLOAD)
    SmugglerAssertions.verify("message", WithStaticClassInitializer.EXTRA_MESSAGE)

    SmugglerAssertions.verify<WithStaticClassInitializer> {
      WithStaticClassInitializer(
          payload = generator.nextString(),
          message = generator.nextString()
      )
    }
  }

  @Test fun shouldWorkWithParcelableArrays() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val text: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Chat(
        val title: String?,
        val participants: Array<User>?,
        val messages: Array<Message>?
    ) : AutoParcelable {
      override fun equals(other: Any?): Boolean {
        if (other == null || other !is Chat) {
          return false
        }

        return TextUtils.equals(title, other.title) &&
            Arrays.equals(participants, other.participants) &&
            Arrays.equals(messages, other.messages)
      }
    }

    SmugglerAssertions.verify<Chat> {
      Chat(
          title = generator.nextNullableString(),
          participants = generator.nextNullableArray {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextNullableArray {
            Message(
                text = generator.nextString(),
                timestamp = generator.nextLong()
            )
          }
      )
    }
  }

  private data class WithStaticClassInitializer(
      val payload: String,
      val message: String
  ) : AutoParcelable {
    companion object {
      const val EXTRA_PAYLOAD = "payload"
      const val EXTRA_MESSAGE = "message"
    }
  }

  private enum class Magic {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
  }

  private enum class Foo {
    FOO, BAR, FOO_FOO, BAR_BAR, FOO_BAR, BAR_FOO
  }
}
