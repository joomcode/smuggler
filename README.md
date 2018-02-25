# Smuggler
Makes your data classes Parcelable. Just add `AutoParcelable` to the class declaration. That's all. No methods need to be implemented.

```kotlin
data class User(
  val uuid: String,
  val firstName: String,
  val lastName: String
) : AutoParcelable

data class Message(
  val uuid: String,
  val text: String,
  val sender: User,
  val timestamp: Long,
  val seen: Boolean
) : AutoParcelable

data class Chat(
  val title: String,
  val messages: List<Message>,
  val participants: List<User>
) : AutoParcelable
```

Kotlin objects are supported as well:
```kotlin
interface Command : AutoParcelable

object LoginCommand : Command
object LogoutCommand : Command
object MenuCommand : Command

data class ProductCommand(val id: String) : Command
data class CategoryCommand(val id: String) : Command
```

# Project Setup
```gradle
buildscript {
  repositories {
    mavenCentral()
    jcenter()
  }

  dependencies {
    classpath "com.android.tools.build:gradle:3.0.1"
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.21"
    classpath "io.mironov.smuggler:smuggler-plugin:0.13.1"
  }
}

repositories {
  mavenCentral()
  jcenter()
}

apply plugin: "com.android.application"
apply plugin: "io.mironov.smuggler"
```
Some **important** notes:
- Smuggler plugin must be applied **after** android plugin.
- In case you have a mutli-module project and want to use `AutoParceable`, you have to add `smuggler-plugin` to each module.

# Supported types
- Primitive types: `boolean`, `byte`, `char`, `double`, `float`, `int`, `long`, `short`
- Boxed types: `Boolean`, `Byte`, `Char`, `Double`, `Float`, `Int`, `Long`, `Short`
- Other types: `String`, `Bundle`, `SparseBooleanArray`, `Date`, `SparseArray`
- All `Parcelable` subclasses
- All `Enum` subclasses
- All `Serializable` subclasses
- `Arrays` of any supported type, including primitive arrays and multidimensional arrays like: `Array<Array<Array<User>>`
- `Sets`, `Lists` and `Maps` of any supported type, including primitive types and complex types like: `Map<List<Message>, List<Array<Set<User>>>`

# Custom types
- Serialization and deserialization of custom types are supported via `TypeAdapter`'s:
  ```kotlin
  object BigIntegerTypeAdapter : TypeAdapter<BigInteger> {
    override fun fromParcel(parcel: Parcel): BigInteger {
      return BigInteger(parcel.createByteArray())
    }

    override fun toParcel(value: BigInteger, parcel: Parcel, flags: Int) {
      parcel.writeByteArray(value.toByteArray())
    }
  }
  ```
- Project-level `TypeAdapter` can be registered using `@GlobalAdapter` annotation:

  ```kotlin
  @GlobalAdapter
  object BigIntegerTypeAdapter : TypeAdapter<BigInteger> {
    ...
  }
  ```
- Class-level `TypeAdapter` can be registered using `@LocalAdapter` annotation:

  ```kotlin
  @LocalAdapter(BigIntegerTypeAdapter::class, CalendarTypeAdapter::class)
  data class Local(
    val integer: BigInteger,
    val calendar: Calendar
  ) : AutoParcelable
  ```
- Defining `TypeAdapter` for a particular type automatically allows to use this type with `Lists`, `Maps`, `Sets` and `Arrays`:

  ```kotlin
  data class BigIntegerExample(
    val single: BigInteger,
    val array: Array<BigInteger>,
    val multidimensional: Array<Array<BigInteger>>,
    val list: List<BigInteger>,
    val set: Set<BigInteger>,
    val map: Map<String, BigInteger>,
    val complex: Map<Set<BigInteger>, Array<List<BigInteger>>>
  ) : AutoParcelable
  ```
- Custom `TypeAdapter` can be defined both as `class` or `object`
- `TypeAdapter` defined as `class` must have a public no-args constructor

# How does it work?
`Smuggler` doesn't use reflection so you don't have to worry about its performance. It isn't an annotation proccessor so you don't have to deal with `kapt` bugs. Instead, `Smuggler` built on top of [transform api](http://tools.android.com/tech-docs/new-build-system/transform-api) and works by proccessing your compiled bytecode and patching classes that implement `AutoParcelable` interface.

# Known limitations
- Only data classes and kotlin objects are supported
- Data classes with type parameters aren't supported at the moment
- Lists, Maps and Arrays with bounded type parameters aren't supported at the moment
- The library doesn't work nicely with JVM tests at the moment [#12](https://github.com/nsk-mironov/smuggler/issues/12)

# What does "Smuggler" mean?
A smuggler was an individual who dealt with the secret exchanged shipment of goods to block restrictions or tax fees. The items shipped were often considered contraband, and highly illegal. Notable smugglers included Han Solo, Chewbacca, and Lando Calrissian. © [http://starwars.wikia.com](http://starwars.wikia.com/wiki/Smuggler)

# License

    Copyright 2016 Vladimir Mironov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
