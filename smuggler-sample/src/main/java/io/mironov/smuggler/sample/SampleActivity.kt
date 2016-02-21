package io.mironov.smuggler.sample

import android.app.Activity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import io.mironov.smuggler.AutoParcelable

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sample)
  }
}

data class User(val firstName: String, val lastName: String) : AutoParcelable {
  companion object CREATOR : Parcelable.Creator<User> {
    override fun newArray(size: Int): Array<out User?> {
      return arrayOfNulls(size)
    }

    override fun createFromParcel(parcel: Parcel): User {
      val firstName = parcel.readString()
      val lastName = parcel.readString()

      return User(firstName, lastName)
    }
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(firstName)
    parcel.writeString(lastName)
  }

  override fun describeContents(): Int {
    return 0
  }
}

data class Message(
    val id: Int,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val magic: Short
) : AutoParcelable
