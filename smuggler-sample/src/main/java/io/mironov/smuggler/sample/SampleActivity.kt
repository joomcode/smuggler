package io.mironov.smuggler.sample

import android.app.Activity
import android.os.Bundle

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sample)
  }
}

data class User(val firstName: String, val userName: String)
data class Message(val text: String, val user: User)
data class Chat(val messages: List<Message>, val participants: List<User>)
