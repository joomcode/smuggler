package io.mironov.smuggler.sample

import android.app.Activity
import android.os.Bundle
import io.mironov.smuggler.AutoParcelable

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sample)
  }
}

data class User(val firstName: String, val userName: String) : AutoParcelable
data class Message(val text: String, val user: User) : AutoParcelable
data class Chat(val title: String, val messages: List<Message>, val participants: List<User>) : AutoParcelable
