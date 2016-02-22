package io.mironov.smuggler.sample

import android.os.Parcel
import io.mironov.smuggler.AutoParcelable
import org.junit.Assert

object SmugglerAssertions {
  fun <P : AutoParcelable> verify(parcelable: P) {
    val marshalled = marshall(parcelable)
    val unmarshalled = unmarshall<User>(marshalled, parcelable.javaClass.classLoader)

    Assert.assertEquals(parcelable, unmarshalled)
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