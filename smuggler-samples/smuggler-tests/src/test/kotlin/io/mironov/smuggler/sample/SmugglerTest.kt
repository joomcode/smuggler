package io.mironov.smuggler.sample

import android.os.Parcel
import android.support.test.runner.AndroidJUnit4
import android.util.SparseArray
import android.util.SparseBooleanArray
import io.mironov.smuggler.AutoParcelable
import io.mironov.smuggler.GlobalAdapter
import io.mironov.smuggler.LocalAdapter
import io.mironov.smuggler.TypeAdapter
import io.mironov.smuggler.library.Chat
import io.mironov.smuggler.library.Message
import io.mironov.smuggler.library.User
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable
import java.math.BigInteger
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.SortedMap
import java.util.TreeMap

@Suppress("EqualsOrHashCode")
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
    ) : AutoParcelable

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
          user = generator.nextNullableValue {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          payload = generator.nextNullableValue {
            Payload(
                text = generator.nextString(),
                timestamp = generator.nextLong()
            )
          },
          status = generator.nextNullableValue {
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
    ) : AutoParcelable

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

  @Test fun shouldWorkWithPrimitiveMultiDimensionalArrays() {
    data class MultiDimensionalPrimitiveArrays(
        val longs: Array<LongArray>?,
        val booleans: Array<BooleanArray>?,
        val bytes: Array<ByteArray>?,
        val chars: Array<Array<CharArray>>?,
        val ints: Array<Array<IntArray>>?,
        val floats: Array<Array<FloatArray>>?,
        val doubles: Array<DoubleArray>?,
        val shorts: Array<ShortArray>?,
        val strings: Array<Array<String>>?
    ) : AutoParcelable

    SmugglerAssertions.verify<MultiDimensionalPrimitiveArrays> {
      MultiDimensionalPrimitiveArrays(
          longs = generator.nextNullableArray { generator.nextLongArray() },
          booleans = generator.nextNullableArray { generator.nextBooleanArray() },
          bytes = generator.nextNullableArray { generator.nextByteArray() },
          chars = generator.nextNullableArray { generator.nextArray { generator.nextCharArray() } },
          ints = generator.nextNullableArray { generator.nextArray { generator.nextIntArray() } },
          floats = generator.nextNullableArray { generator.nextArray { generator.nextFloatArray() } },
          doubles = generator.nextNullableArray { generator.nextDoubleArray() },
          shorts = generator.nextNullableArray { generator.nextShortArray() },
          strings = generator.nextNullableArray { generator.nextStringArray() }
      )
    }
  }

  @Test fun shouldWorkWithBoxedMultiDimensionalArrays() {
    data class MultiDimensionalBoxedArrays(
        val longs: Array<Array<Long>>?,
        val booleans: Array<Array<Boolean>>?,
        val bytes: Array<Array<Byte>>?,
        val chars: Array<Array<Array<Char>>>?,
        val ints: Array<Array<Array<Int>>>?,
        val floats: Array<Array<Array<Float>>>?,
        val doubles: Array<Array<Double>>?,
        val shorts: Array<Array<Short>>?,
        val strings: Array<Array<String>>?
    ) : AutoParcelable

    SmugglerAssertions.verify<MultiDimensionalBoxedArrays> {
      MultiDimensionalBoxedArrays(
          longs = generator.nextNullableArray { generator.nextArray { generator.nextLong() } },
          booleans = generator.nextNullableArray { generator.nextArray { generator.nextBoolean() } },
          bytes = generator.nextNullableArray { generator.nextArray { generator.nextByte() } },
          chars = generator.nextNullableArray { generator.nextArray { generator.nextArray { generator.nextChar() } } },
          ints = generator.nextNullableArray { generator.nextArray { generator.nextArray { generator.nextInt() } } },
          floats = generator.nextNullableArray { generator.nextArray { generator.nextArray { generator.nextFloat() } } },
          doubles = generator.nextNullableArray { generator.nextArray { generator.nextDouble() } },
          shorts = generator.nextNullableArray { generator.nextArray { generator.nextShort() } },
          strings = generator.nextNullableArray { generator.nextStringArray() }
      )
    }
  }

  @Test fun shouldWorkWithSerializable() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : Serializable

    data class Payload(
        val message: String,
        val timestamp: Long
    ) : Serializable

    data class Message(
        val sender: User,
        val payload: Payload
    ) : Serializable

    data class Chat(
        val title: String,
        val participants: Array<User>,
        val messages: Array<Message>
    ) : AutoParcelable

    SmugglerAssertions.verify<Chat>() {
      Chat(
          title = generator.nextString(),
          participants = generator.nextArray {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextArray {
            Message(
                sender = User(
                    firstName = generator.nextString(),
                    lastName = generator.nextString()
                ),
                payload = Payload(
                    message = generator.nextString(),
                    timestamp = generator.nextLong()
                )
            )
          }
      )
    }
  }

  @Test fun shouldWorkWithSparseArrays() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val message: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Simple(
        val booleans: SparseBooleanArray,
        val strings: SparseArray<String>,
        val longs: SparseArray<Long>,
        val doubles: SparseArray<Double>,
        val floats: SparseArray<Float>
    ) : AutoParcelable

    data class Complex(
        val users: SparseArray<User>,
        val messages: SparseArray<Message>
    ) : AutoParcelable

    SmugglerAssertions.verify<Simple>() {
      Simple(
          booleans = generator.nextSparseBooleanArray(),
          strings = generator.nextSparseArray { generator.nextString() },
          longs = generator.nextSparseArray { generator.nextLong() },
          doubles = generator.nextSparseArray { generator.nextDouble() },
          floats = generator.nextSparseArray { generator.nextFloat() }
      )
    }

    SmugglerAssertions.verify<Complex>() {
      Complex(
          users = generator.nextSparseArray {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextSparseArray {
            Message(
                message = generator.nextString(),
                timestamp = generator.nextLong()
            )
          }
      )
    }
  }

  @Test fun shouldWorkWithLists() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val message: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Lists(
        val users: List<User>,
        val messages: List<Message>,
        val booleans: List<Boolean>,
        val strings: List<String>,
        val longs: List<Long>
    ) : AutoParcelable

    SmugglerAssertions.verify<Lists>() {
      Lists(
          users = generator.nextList {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextList {
            Message(
                message = generator.nextString(),
                timestamp = generator.nextLong()
            )
          },
          booleans = generator.nextList { generator.nextBoolean() },
          strings = generator.nextList { generator.nextString() },
          longs = generator.nextList { generator.nextLong() }
      )
    }
  }

  @Test fun shouldWorkWithComplexListsAndArrays() {
    data class ComplexArraysAndLists(
        val one: List<List<List<Long>>>,
        val two: List<Array<List<Boolean>>>,
        val three: List<Array<Array<Boolean>>>,
        val four: Array<List<Array<Boolean>>>
    ) : AutoParcelable

    SmugglerAssertions.verify<ComplexArraysAndLists>() {
      ComplexArraysAndLists(
          one = generator.nextList { generator.nextList { generator.nextList { generator.nextLong() } } },
          two = generator.nextList { generator.nextArray { generator.nextList { generator.nextBoolean() } } },
          three = generator.nextList { generator.nextArray { generator.nextArray { generator.nextBoolean() } } },
          four = generator.nextArray { generator.nextList { generator.nextArray { generator.nextBoolean() } } }
      )
    }
  }

  @Test fun shouldWorkWithSets() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val message: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Simple(
        val users: Set<User>,
        val messages: Set<Message>,
        val booleans: Set<Boolean>,
        val strings: Set<String>,
        val longs: Set<Long>
    ) : AutoParcelable

    data class Complex(
        val one: Set<Set<Long>>,
        val two: Set<Array<Boolean>>,
        val three: Set<List<Boolean>>,
        val four: Array<Set<Boolean>>,
        val five: List<Set<Boolean>>
    ) : AutoParcelable

    SmugglerAssertions.verify<Simple>() {
      Simple(
          users = generator.nextSet {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextSet {
            Message(
                message = generator.nextString(),
                timestamp = generator.nextLong()
            )
          },
          booleans = generator.nextSet { generator.nextBoolean() },
          strings = generator.nextSet { generator.nextString() },
          longs = generator.nextSet { generator.nextLong() }
      )
    }

    SmugglerAssertions.verify<Complex>() {
      Complex(
          one = generator.nextSet { generator.nextSet { generator.nextLong() } },
          two = generator.nextSet { generator.nextArray { generator.nextBoolean() } } ,
          three = generator.nextSet { generator.nextList { generator.nextBoolean() } },
          four = generator.nextArray { generator.nextSet { generator.nextBoolean() } },
          five = generator.nextList { generator.nextSet { generator.nextBoolean() } }
      )
    }
  }

  @Test fun shouldWorkWithMaps() {
    data class User(
        val firstName: String,
        val lastName: String
    ) : AutoParcelable

    data class Message(
        val message: String,
        val timestamp: Long
    ) : AutoParcelable

    data class Maps(
        val one: Map<String, String>,
        val two: Map<String, Array<String>>,
        val three: Map<Int, Boolean>,
        val four: Map<Int, User>,
        val five: Map<Int, Array<Message>>,
        val six: Map<Long, List<Message>>,
        val seven: Map<List<Message>, List<User>>
    ) : AutoParcelable

    SmugglerAssertions.verify<Maps> {
      Maps(
          one = generator.nextMap({ generator.nextString() }, { generator.nextString() }),
          two = generator.nextMap({ generator.nextString() }, { generator.nextArray { generator.nextString() } }),
          three = generator.nextMap({ generator.nextInt() }, { generator.nextBoolean() }),
          four = generator.nextMap({ generator.nextInt() }, {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          }),
          five = generator.nextMap({ generator.nextInt() }, {
            generator.nextArray {
              Message(
                  message = generator.nextString(),
                  timestamp = generator.nextLong()
              )
            }
          }),
          six = generator.nextMap({ generator.nextLong() }, {
            generator.nextList {
              Message(
                  message = generator.nextString(),
                  timestamp = generator.nextLong()
              )
            }
          }),
          seven = generator.nextMap(
              key = {
                generator.nextList {
                  Message(
                      message = generator.nextString(),
                      timestamp = generator.nextLong()
                  )
                }
              },
              value = {
                generator.nextList {
                  User(
                      firstName = generator.nextString(),
                      lastName = generator.nextString()
                  )
                }
              }
          )
      )
    }
  }

  @Test fun shouldWorkWithMapSubclasses() {
    data class Maps(
        val base: Map<String, String>,
        val hash: HashMap<String, String>,
        val linked: LinkedHashMap<String, String>,
        val sorted: SortedMap<String, String>,
        val tree: TreeMap<String, String>
    ) : AutoParcelable

    SmugglerAssertions.verify<Maps>() {
      Maps(
          base = generator.nextMap({ LinkedHashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          hash = generator.nextMap({ HashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          linked = generator.nextMap({ LinkedHashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          sorted = generator.nextMap({ TreeMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          tree = generator.nextMap({ TreeMap<String, String>() }, { generator.nextString() }, { generator.nextString() })
      )
    }
  }

  @Test fun shouldWorkWithPrivateProperties() {
    data class Numbers(
        private val one: String,
        private val two: String,
        private val three: String,
        val four: String,
        val five: String,
        val six: String
    ) : AutoParcelable

    SmugglerAssertions.verify<Numbers> {
      Numbers(
          one = generator.nextString(),
          two = generator.nextString(),
          three = generator.nextString(),
          four = generator.nextString(),
          five = generator.nextString(),
          six = generator.nextString()
      )
    }
  }

  @Test fun shouldWorkWithLibraryProjects() {
    SmugglerAssertions.verify<Chat>() {
      Chat(
          title = generator.nextString(),
          participants = generator.nextList {
            User(
                firstName = generator.nextString(),
                lastName = generator.nextString()
            )
          },
          messages = generator.nextList {
            Message(
                text = generator.nextString(),
                seen = generator.nextBoolean()
            )
          }
      )
    }
  }

  @Test fun shouldWorkWithDates() {
    data class Sample(
        val start: Date,
        val end: Date
    ) : AutoParcelable

    SmugglerAssertions.verify<Sample>() {
      Sample(
          start = Date(generator.nextLong()),
          end = Date(generator.nextLong())
      )
    }
  }

  @Test fun shouldWorkWithGlobalAdapters() {
    data class Global(
        val integer: BigInteger,
        val calendar: Calendar,
        val integers: List<BigInteger>,
        val calendars: List<Calendar>
    ) : AutoParcelable

    SmugglerAssertions.verify<Global>() {
      Global(
          integer = BigInteger(generator.nextInt().toString()),
          calendar = Calendar.getInstance().apply { timeInMillis = generator.nextLong() },
          integers = generator.nextList { BigInteger(generator.nextInt().toString()) },
          calendars = generator.nextList { Calendar.getInstance().apply { timeInMillis = generator.nextLong() } }
      )
    }
  }

  @Test fun shouldWorkWithLocalAdapters() {
    data class Timestamp(val millis: Long)
    data class Date(val millis: Long)

    class TimestampTypeAdapter : TypeAdapter<Timestamp> {
      override fun fromParcel(parcel: Parcel): Timestamp {
        return Timestamp(parcel.readLong())
      }

      override fun toParcel(value: Timestamp, parcel: Parcel, flags: Int) {
        parcel.writeLong(value.millis)
      }
    }

    class DateTypeAdapter : TypeAdapter<Date> {
      override fun fromParcel(parcel: Parcel): Date {
        return Date(parcel.readLong())
      }

      override fun toParcel(value: Date, parcel: Parcel, flags: Int) {
        parcel.writeLong(value.millis)
      }
    }

    @LocalAdapter(TimestampTypeAdapter::class, DateTypeAdapter::class)
    data class Local(
        val timestamp: Timestamp,
        val date: Date
    ) : AutoParcelable

    SmugglerAssertions.verify<Local>() {
      Local(
          timestamp = Timestamp(generator.nextLong()),
          date = Date(generator.nextLong())
      )
    }
  }

  @GlobalAdapter
  object BigIntegerTypeAdapter : TypeAdapter<BigInteger> {
    override fun fromParcel(parcel: Parcel): BigInteger {
      return BigInteger(parcel.createByteArray())
    }

    override fun toParcel(value: BigInteger, parcel: Parcel, flags: Int) {
      parcel.writeByteArray(value.toByteArray())
    }
  }

  @GlobalAdapter
  object CalendarTypeAdapter : TypeAdapter<Calendar> {
    override fun fromParcel(parcel: Parcel): Calendar {
      return Calendar.getInstance().apply {
        timeInMillis = parcel.readLong()
      }
    }

    override fun toParcel(value: Calendar, parcel: Parcel, flags: Int) {
      parcel.writeLong(value.timeInMillis)
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
