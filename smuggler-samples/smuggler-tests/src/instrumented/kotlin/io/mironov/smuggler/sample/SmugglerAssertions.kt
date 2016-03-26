package io.mironov.smuggler.sample

import android.os.Parcel
import io.mironov.smuggler.AutoParcelable
import org.junit.Assert

object SmugglerAssertions {
  inline fun <reified P : AutoParcelable> verify(factory: () -> P) {
    verify(P::class.java)

    0.until(25).forEach {
      verify(factory())
    }
  }

  fun <P : AutoParcelable> verify(parcelable: P) {
    val marshalled = marshall(parcelable)
    val unmarshalled = unmarshall<P>(marshalled, parcelable.javaClass.classLoader)

    Assert.assertTrue(SmugglerEquivalence.equals(parcelable, unmarshalled))
  }

  fun <P : AutoParcelable> verify(clazz: Class<P>) {
    consume(AutoParcelable.creator(clazz))
  }

  fun verify(expected: Any?, actual: Any?) {
    Assert.assertEquals(expected, actual)
  }

  fun consume(any: Any?) {
    // nothing to do
  }

  private fun <P : AutoParcelable> marshall(parcelable: P): ByteArray {
    val parcel = Parcel.obtain().apply {
      writeParcelable(parcelable, 0)
    }

    return parcel.marshall().apply {
      parcel.recycle()
    }
  }

  private fun <P : AutoParcelable> unmarshall(bytes: ByteArray, loader: ClassLoader): P {
    val parcel = Parcel.obtain().apply {
      unmarshall(bytes, 0, bytes.size)
      setDataPosition(0)
    }

    return parcel.readParcelable<P>(loader).apply {
      parcel.recycle()
    }
  }
}
