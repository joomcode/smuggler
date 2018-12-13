package io.mironov.smuggler.sample

import android.os.Parcel
import android.os.Parcelable
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable
import java.math.BigInteger
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.LinkedList
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

@Suppress("ArrayInDataClass")
@RunWith(AndroidJUnit4::class)
class SmugglerTest {
  private lateinit var generator: SmugglerGenerator

  @Before
  fun setUp() {
    generator = SmugglerGenerator(9999)
  }

  @Test
  fun shouldWorkWithPrimitives() {
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

  @Test
  fun shouldWorkWithNestedObjects() {
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

  @Test
  fun shouldWorkWithPrimitiveArrays() {
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

  @Test
  fun shouldWorkWithEnums() {
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

  @Test
  fun shouldWorkWithOptionalPrimitives() {
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

  @Test
  fun shouldWorkWithOptionalNestedObjects() {
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

  @Test
  fun shouldWorkWithCustomStaticClassInitializer() {
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

  @Test
  fun shouldWorkWithParcelableArrays() {
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

  @Test
  fun shouldWorkWithPrimitiveMultiDimensionalArrays() {
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

  @Test
  fun shouldWorkWithBoxedMultiDimensionalArrays() {
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

  @Test
  fun shouldWorkWithSerializable() {
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

    SmugglerAssertions.verify<Chat> {
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

  @Test
  fun shouldWorkWithSparseArrays() {
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

    SmugglerAssertions.verify<Simple> {
      Simple(
          booleans = generator.nextSparseBooleanArray(),
          strings = generator.nextSparseArray { generator.nextString() },
          longs = generator.nextSparseArray { generator.nextLong() },
          doubles = generator.nextSparseArray { generator.nextDouble() },
          floats = generator.nextSparseArray { generator.nextFloat() }
      )
    }

    SmugglerAssertions.verify<Complex> {
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

  @Test
  fun shouldWorkWithLists() {
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

    SmugglerAssertions.verify<Lists> {
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

  @Test
  fun shouldWorkWithListSubclasses() {
    data class Lists(
        val base: List<String>,
        val array: ArrayList<String>,
        val linked: LinkedList<String>
    ) : AutoParcelable

    SmugglerAssertions.verify<Lists> {
      Lists(
          base = generator.nextList { generator.nextString() },
          array = generator.nextList({ ArrayList<String>() }, { generator.nextString() }),
          linked = generator.nextList({ LinkedList<String>() }, { generator.nextString() })
      )
    }
  }

  @Test
  fun shouldWorkWithComplexListsAndArrays() {
    data class ComplexArraysAndLists(
        val one: List<List<List<Long>>>,
        val two: List<Array<List<Boolean>>>,
        val three: List<Array<Array<Boolean>>>,
        val four: Array<List<Array<Boolean>>>,
        val five: Collection<Collection<String>>
    ) : AutoParcelable

    SmugglerAssertions.verify<ComplexArraysAndLists> {
      ComplexArraysAndLists(
          one = generator.nextList { generator.nextList { generator.nextList { generator.nextLong() } } },
          two = generator.nextList { generator.nextArray { generator.nextList { generator.nextBoolean() } } },
          three = generator.nextList { generator.nextArray { generator.nextArray { generator.nextBoolean() } } },
          four = generator.nextArray { generator.nextList { generator.nextArray { generator.nextBoolean() } } },
          five = generator.nextList { generator.nextList { generator.nextString() } }
      )
    }
  }

  @Test
  fun shouldWorkWithSets() {
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

    SmugglerAssertions.verify<Simple> {
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

    SmugglerAssertions.verify<Complex> {
      Complex(
          one = generator.nextSet { generator.nextSet { generator.nextLong() } },
          two = generator.nextSet { generator.nextArray { generator.nextBoolean() } },
          three = generator.nextSet { generator.nextList { generator.nextBoolean() } },
          four = generator.nextArray { generator.nextSet { generator.nextBoolean() } },
          five = generator.nextList { generator.nextSet { generator.nextBoolean() } }
      )
    }
  }

  @Test
  fun shouldWorkWithSetSubclasses() {
    data class Sets(
        val base: Set<String>,
        val hash: HashSet<String>,
        val linked: LinkedHashSet<String>,
        val sorted: SortedSet<String>,
        val tree: TreeSet<String>
    ) : AutoParcelable

    SmugglerAssertions.verify<Sets> {
      Sets(
          base = generator.nextSet({ LinkedHashSet<String>() }, { generator.nextString() }),
          hash = generator.nextSet({ HashSet<String>() }, { generator.nextString() }),
          linked = generator.nextSet({ LinkedHashSet<String>() }, { generator.nextString() }),
          sorted = generator.nextSet({ TreeSet<String>() }, { generator.nextString() }),
          tree = generator.nextSet({ TreeSet<String>() }, { generator.nextString() })
      )
    }
  }

  @Test
  fun shouldWorkWithMaps() {
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

  @Test
  fun shouldWorkWithMapSubclasses() {
    data class Maps(
        val base: Map<String, String>,
        val hash: HashMap<String, String>,
        val linked: LinkedHashMap<String, String>,
        val sorted: SortedMap<String, String>,
        val tree: TreeMap<String, String>
    ) : AutoParcelable

    SmugglerAssertions.verify<Maps> {
      Maps(
          base = generator.nextMap({ LinkedHashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          hash = generator.nextMap({ HashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          linked = generator.nextMap({ LinkedHashMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          sorted = generator.nextMap({ TreeMap<String, String>() }, { generator.nextString() }, { generator.nextString() }),
          tree = generator.nextMap({ TreeMap<String, String>() }, { generator.nextString() }, { generator.nextString() })
      )
    }
  }

  @Test
  fun shouldWorkWithPrivateProperties() {
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

  @Test
  fun shouldWorkWithLibraryProjects() {
    SmugglerAssertions.verify<Chat> {
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

  @Test
  fun shouldWorkWithDates() {
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

  @Test
  fun shouldWorkWithGlobalAdapters() {
    data class Global(
        val integer: BigInteger,
        val calendar: Calendar,
        val integers: List<BigInteger>,
        val calendars: List<Calendar>
    ) : AutoParcelable

    SmugglerAssertions.verify<Global> {
      Global(
          integer = BigInteger(generator.nextInt().toString()),
          calendar = Calendar.getInstance().apply { timeInMillis = generator.nextLong() },
          integers = generator.nextList { BigInteger(generator.nextInt().toString()) },
          calendars = generator.nextList { Calendar.getInstance().apply { timeInMillis = generator.nextLong() } }
      )
    }
  }

  @Test
  fun shouldWorkWithLocalAdapters() {
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

  @Test
  fun shouldWorkWithPolymorphicCollections() {
    data class Foo(override val value: String) : Wrapper
    data class Bar(override val value: String) : Wrapper

    data class Collection(
        val values: List<Wrapper>
    ) : AutoParcelable

    SmugglerAssertions.verify<Collection> {
      Collection(listOf(
          Foo(generator.nextString()),
          Bar(generator.nextString())
      ))
    }
  }

  @Test
  fun shouldWorkWithObjects() {
    data class Objects(
        val one: LoginCommand,
        val two: LogoutCommand,
        val three: MenuCommand
    ) : AutoParcelable

    data class Lists(
        val one: List<LoginCommand>,
        val two: List<LogoutCommand>,
        val three: List<MenuCommand>,
        val four: List<Command>
    ) : AutoParcelable

    data class Sets(
        val one: Set<LoginCommand>,
        val two: Set<LogoutCommand>,
        val three: Set<MenuCommand>,
        val four: Set<Command>
    ) : AutoParcelable

    SmugglerAssertions.verify<Objects> {
      Objects(
          one = LoginCommand,
          two = LogoutCommand,
          three = MenuCommand
      )
    }

    SmugglerAssertions.verify<Lists> {
      Lists(
          one = generator.nextList { LoginCommand },
          two = generator.nextList { LogoutCommand },
          three = generator.nextList { MenuCommand },
          four = generator.nextList { generator.nextElement(arrayOf(LoginCommand, LogoutCommand, ProductCommand(generator.nextString()), CategoryCommand(generator.nextString()))) }
      )
    }

    SmugglerAssertions.verify<Sets> {
      Sets(
          one = generator.nextSet { LoginCommand },
          two = generator.nextSet { LogoutCommand },
          three = generator.nextSet { MenuCommand },
          four = generator.nextSet { generator.nextElement(arrayOf(LoginCommand, LogoutCommand, ProductCommand(generator.nextString()), CategoryCommand(generator.nextString()))) }
      )
    }
  }

  @Test
  fun shouldWorkWithCharSequence() {
    data class Container(
        val value: CharSequence,
        val collection: Collection<CharSequence>,
        val array: Array<CharSequence>,
        val list: List<CharSequence>
    ) : AutoParcelable

    SmugglerAssertions.verify<Container> {
      Container(
          value = generator.nextString(),
          collection = generator.nextList { generator.nextString() },
          array = generator.nextArray { generator.nextString() },
          list = generator.nextList { generator.nextString() }
      )
    }
  }

  @Test
  fun shouldWorkWithSealedClasses() {
    SmugglerAssertions.verify<Result.Success> {
      Result.Success(value = generator.nextString())
    }

    SmugglerAssertions.verify<Result.Failure> {
      Result.Failure(code = generator.nextInt())
    }

    SmugglerAssertions.verify<Result>(strict = false) {
      if (generator.nextBoolean()) {
        Result.Success(value = generator.nextString())
      } else {
        Result.Failure(code = generator.nextInt())
      }
    }
  }

  @Test
  fun shouldWorkWithManualCreator() {
    data class Container(
        val single: Manual,
        val array: Array<Manual>,
        val list: List<Manual>,
        val set: Set<Manual>
    ) : AutoParcelable

    SmugglerAssertions.verify<Manual> {
      Manual(value = generator.nextInt())
    }

    SmugglerAssertions.verify<Container> {
      Container(
          single = Manual(generator.nextInt()),
          array = generator.nextArray { Manual(generator.nextInt()) },
          list = generator.nextList { Manual(generator.nextInt()) },
          set = generator.nextSet { Manual(generator.nextInt()) }
      )
    }
  }

  @Test
  fun shouldOptimizeRequiredFields() {
    data class Foo(
        val field: Int
    ) : AutoParcelable

    data class Required(
        val field: Foo
    ) : AutoParcelable

    data class Optional(
        val field: Foo?
    ) : AutoParcelable

    SmugglerAssertions.verify {
      val field = Foo(generator.nextInt())

      val optional = SmugglerAssertions.size(Optional(field))
      val required = SmugglerAssertions.size(Required(field))

      Assert.assertEquals(4, optional - required)
    }
  }

  @Test
  fun shouldOptimizeRequiredTypeArguments() {
    data class Required(
        val list: List<Int>,
        val nested: List<List<Int>>
    ) : AutoParcelable

    data class Optional(
        val list: List<Int?>,
        val nested: List<List<Int?>?>
    ) : AutoParcelable

    SmugglerAssertions.verify {
      val list = generator.nextList { generator.nextInt() }
      val nested = generator.nextList { list }

      val optional = SmugglerAssertions.size(Optional(list, nested))
      val required = SmugglerAssertions.size(Required(list, nested))

      Assert.assertEquals(4 * (list.size + nested.size + nested.size * list.size), optional - required)
    }
  }

  private class Manual(val value: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeInt(value)
    }

    override fun describeContents(): Int {
      return 0
    }

    override fun equals(other: Any?): Boolean {
      return other is Manual && other.value == value
    }

    override fun hashCode(): Int {
      return value
    }

    override fun toString(): String {
      return "Manual[value = $value]"
    }

    companion object CREATOR : Parcelable.Creator<Manual> {
      override fun createFromParcel(parcel: Parcel): Manual {
        return Manual(parcel.readInt())
      }

      override fun newArray(size: Int): Array<Manual?> {
        return arrayOfNulls(size)
      }
    }
  }

  private sealed class Result(val type: String) : AutoParcelable {
    class Success(val value: String) : Result("success")
    class Failure(val code: Int) : Result("failure")
  }

  interface Wrapper : AutoParcelable {
    val value: String
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

  private interface Command : AutoParcelable

  private object LoginCommand : Command
  private object LogoutCommand : Command
  private object MenuCommand : Command

  private data class ProductCommand(val id: String) : Command
  private data class CategoryCommand(val id: String) : Command

  private enum class Magic {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
  }

  private enum class Foo {
    FOO, BAR, FOO_FOO, BAR_BAR, FOO_BAR, BAR_FOO
  }
}
