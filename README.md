# Smuggler
Makes your data classes Parcelable. Just add `AutoParceable` to the class declaration. That's all. No methods need to be implemented.

```kotlin
data class User(
  val firstName: String,
  val lastName: String
) : AutoParcelable

data class Message(
  val text: String,
  val user: User
) : AutoParcelable

data class Chat(
  val title: String,
  val messages: List<Message>,
  val participants: List<User>
) : AutoParcelable
```

# Project Setup
```gradle
buildscript {
  repositories {
    mavenCentral()
    jcenter()
  }
    
  dependencies {
  	classpath "com.android.tools.build:gradle:1.5.0"
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.0.0"
    classpath "io.mironov.smuggler:smuggler-plugin:0.12.3"
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
- Only android gradle plugin 1.5.0 and up is supported.
- In case you have a mutli-module project and want to use `AutoParceable`, you have to add `smuggler-plugin` to each module.
- Smuggler plugin must be applied **after** android plugin.

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

# Known limitations
- Only data classes are supported
- Data classes with type parameters aren't supported at the moment
- Lists, Maps and Arrays with bounded type parameters aren't supported at the moment
- Jack&Jill toolchain isn't supported at the moment

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
