package io.mironov.smuggler.sample

import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmugglerTest {
  @Test fun shouldFailWithUnsupportedOperationException() {
    throw UnsupportedOperationException()
  }
}
