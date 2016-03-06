# Smuggler
Makes your data classes Parcelable. Just add `AutoParceable` to the class declaration. That's all. No methods need to be implemented.

```kotlin
data class User(
  val firstName: String,
  val userName: String
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
### Add JCenter Repository
Make sure you have `jcenter()` repository in your root `build.gradle`:
```gradle
buildscript {
  repositories {
    ...
    jcenter()
  }
}

allprojects {
  buildscript {
    repositories {
      ...
      jcenter()
    }
  }

  repositories {
    ...
    jcenter()
  }
}
```
### Add Smuggler plugin
```gradle
buildscript {
  dependencies {
  	classpath "com.android.tools.build:gradle:1.5.0"
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.0.0"
    classpath "io.mironov.smuggler:smuggler-plugin:0.10.0"
  }
}
```
**Important note #1**: Only android gradle plugin 1.5.0 and up is supported.

**Important note #2:** In case you are using android gradle plugin 2.0.0, you have to use an experimental version of smuggler plugin: `io.mironov.smuggler:smuggler-plugin-experimental:0.10.0`

**Important note #3**: In case you have a mutli-module project and want to use `AutoParceable`, you have to add `smuggler-plugin` to each module.
### Apply Smuggler plugin
```gradle
apply plugin: "com.android.application"
apply plugin: "io.mironov.smuggler"
```
**Important note #1**: Smuggler plugin must be applied **after** android plugin.

**Improtant note #2**: Experimental plugin has the same id, so you don't need to add `-experimental` suffix.


# Known limitations
- Only data classes are supported
- Data classes with type parameters aren't supported at the moment
- Lists, Maps and Arrays with bounded type parameters aren't supported at the moment
- Custom type adapters aren't supported at the moment

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
