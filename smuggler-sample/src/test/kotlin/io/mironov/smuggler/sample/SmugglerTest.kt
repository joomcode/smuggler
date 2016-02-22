package io.mironov.smuggler.sample

import android.support.test.runner.AndroidJUnit4
import io.mironov.smuggler.AutoParcelable
import org.junit.Test
import org.junit.runner.RunWith

data class User(
    val firstName: String,
    val lastName: String
) : AutoParcelable

@RunWith(AndroidJUnit4::class)
class SmugglerTest {
  @Test fun shouldWorkWithSimpleObject() {
    SmugglerAssertions.verify(User("Darth", "Vader"))
  }
}
