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

# Known limitations
- Only data classes are supported
- Data classes with type parameters aren't supported at the moment
- Lists, Maps and Arrays with bounded type parameters aren't supported at the moment
- Custom type adapters aren't supported at the moment

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
