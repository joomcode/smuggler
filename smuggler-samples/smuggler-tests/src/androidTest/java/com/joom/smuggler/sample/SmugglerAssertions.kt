package com.joom.smuggler.sample

import android.os.Parcel
import android.os.Parcelable
import org.junit.Assert

object SmugglerAssertions {
  inline fun <reified P : Parcelable> verify(strict: Boolean = true, factory: () -> P) {
    if (strict) {
      verify(P::class.java)
    }

    repeat(10) {
      verify(factory())
    }
  }

  inline fun verify(action: () -> Unit) {
    repeat(10) {
      action()
    }
  }

  fun <P : Parcelable> verify(parcelable: P) {
    val marshalled = marshall(parcelable)
    val unmarshalled = unmarshall<P>(marshalled, parcelable.javaClass.classLoader)

    Assert.assertTrue(SmugglerEquivalence.equals(parcelable, unmarshalled))
  }

  fun <P : Parcelable> verify(clazz: Class<P>) {
    consume(clazz.getDeclaredField("CREATOR"))
  }

  fun verify(expected: Any?, actual: Any?) {
    Assert.assertEquals(expected, actual)
  }

  fun consume(any: Any?) {
    // nothing to do
  }

  fun size(parcelable: Parcelable): Int {
    return acquireParcel {
      parcelable.writeToParcel(it, 0)
      it.dataSize()
    }
  }

  private fun <P : Parcelable> marshall(parcelable: P): ByteArray {
    return acquireParcel {
      it.writeParcelable(parcelable, 0)
      it.marshall()
    }
  }

  private fun <P : Parcelable> unmarshall(bytes: ByteArray, loader: ClassLoader): P {
    return acquireParcel {
      it.unmarshall(bytes, 0, bytes.size)
      it.setDataPosition(0)
      it.readParcelable(loader)!!
    }
  }

  private fun <T> acquireParcel(action: (Parcel) -> T): T {
    val parcel = Parcel.obtain()

    return try {
      action(parcel)
    } finally {
      parcel.recycle()
    }
  }
}
