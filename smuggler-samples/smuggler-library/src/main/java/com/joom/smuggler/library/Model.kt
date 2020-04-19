package com.joom.smuggler.library

import com.joom.smuggler.AutoParcelable

data class User(
  var firstName: String,
  var lastName: String
) : AutoParcelable

data class Message(
  var text: String,
  var seen: Boolean
) : AutoParcelable

data class Chat(
  var title: String,
  var participants: List<User>,
  var messages: List<Message>
) : AutoParcelable
