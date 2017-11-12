package io.mironov.smuggler.sample

import android.os.Parcel
import android.os.Parcelable
import io.mironov.smuggler.AutoParcelable
import org.junit.Assert

object SmugglerAssertions {
  inline fun <reified P : Parcelable> verify(strict: Boolean = true, factory: () -> P) {
    if (strict) {
      verify(P::class.java)
    }

    0.until(10).forEach {
      verify(factory())
    }
  }

  fun <P : Parcelable> verify(parcelable: P) {
    val marshalled = marshall(parcelable)
    val unmarshalled = unmarshall<P>(marshalled, parcelable.javaClass.classLoader)

    Assert.assertTrue(SmugglerEquivalence.equals(parcelable, unmarshalled))
  }

  fun <P : Parcelable> verify(clazz: Class<P>) {
    consume(AutoParcelable.creator(clazz))
  }

  fun verify(expected: Any?, actual: Any?) {
    Assert.assertEquals(expected, actual)
  }

  fun consume(any: Any?) {
    // nothing to do
  }

  private fun <P : Parcelable> marshall(parcelable: P): ByteArray {
    val parcel = Parcel.obtain().apply {
      writeParcelable(parcelable, 0)
    }

    return parcel.marshall().apply {
      parcel.recycle()
    }
  }

  private fun <P : Parcelable> unmarshall(bytes: ByteArray, loader: ClassLoader): P {
    val parcel = Parcel.obtain().apply {
      unmarshall(bytes, 0, bytes.size)
      setDataPosition(0)
    }

    return parcel.readParcelable<P>(loader).apply {
      parcel.recycle()
    }
  }
}
