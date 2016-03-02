package io.mironov.smuggler.sample

import android.os.Parcel
import android.os.Parcelable
import io.mironov.smuggler.AutoParcelable
import org.junit.Assert
import java.lang.reflect.Modifier

object SmugglerAssertions {
  inline fun <reified P : AutoParcelable> verify(factory: () -> P) {
    verify(P::class.java)

    for (i in 0..100 - 1) {
      verify(factory())
    }
  }

  fun <P : AutoParcelable> verify(parcelable: P) {
    val marshalled = marshall(parcelable)
    val unmarshalled = unmarshall<P>(marshalled, parcelable.javaClass.classLoader)

    Assert.assertTrue(SmugglerEquivalence.equals(parcelable, unmarshalled))
  }

  fun <P : AutoParcelable> verify(clazz: Class<P>) {
    val creator = try {
      clazz.getField("CREATOR")
    } catch (exception: NoSuchFieldException) {
      throw AssertionError("Parcelable protocol requires a Parcelable.Creator object called CREATOR on class ${clazz.name}")
    }

    if (!Modifier.isStatic(creator.modifiers)) {
      throw AssertionError("Parcelable protocol requires the CREATOR object to be static on class ${clazz.name}")
    }

    if (!Modifier.isPublic(creator.modifiers)) {
      throw AssertionError("Parcelable protocol requires the CREATOR object to be public on class ${clazz.name}")
    }

    if (!Parcelable.Creator::class.java.isAssignableFrom(creator.type)) {
      throw AssertionError("Parcelable protocol requires a Parcelable.Creator object called CREATOR on class ${clazz.name}")
    }
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
