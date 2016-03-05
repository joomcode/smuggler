package io.mironov.smuggler.library

import io.mironov.smuggler.AutoParcelable

data class User(
    val firstName: String,
    val lastName: String
) : AutoParcelable

data class Message(
    val text: String,
    val timestamp: Long,
    val seen: Boolean
) : AutoParcelable

data class Chat(
    val title: String,
    val participants: List<User>,
    val messages: List<Message>
) : AutoParcelable
