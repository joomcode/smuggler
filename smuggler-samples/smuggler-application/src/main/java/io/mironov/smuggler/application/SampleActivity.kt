package io.mironov.smuggler.application

import android.app.Activity
import android.os.Bundle
import io.mironov.smuggler.library.Message
import io.mironov.smuggler.library.User

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sample_activity)

    if (savedInstanceState == null) {
      fragmentManager.beginTransaction()
          .add(R.id.container, SampleFragment.newInstance(
              SampleArguments(
                  user = User(
                      firstName = "Darth",
                      lastName = "Vader"
                  ),
                  message = Message(
                      text = "I am your father",
                      seen = true
                  )
              )
          ))
          .commit()
    }
  }
}
