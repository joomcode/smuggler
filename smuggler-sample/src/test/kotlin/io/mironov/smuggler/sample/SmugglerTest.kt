package io.mironov.smuggler.sample

import android.support.test.runner.AndroidJUnit4
import io.mironov.smuggler.AutoParcelable
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmugglerTest {
  @Test fun shouldWorkWithPrimitives() {
    data class Primitives(
        val boolean: Boolean,
        val byte: Byte,
        val char: Char,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double,
        val string: String
    ) : AutoParcelable

    SmugglerAssertions.verify(Primitives(false, 12, 'a', 523, 55114, 968456312, 1235f, 2151.6, "Magic"))
  }
}
