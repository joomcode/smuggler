package io.mironov.smuggler.sample

import android.app.Activity
import android.os.Bundle
import android.os.Parcel
import io.mironov.smuggler.AutoParcelable

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sample)
  }
}

data class User(val firstName: String, val lastName: String) : AutoParcelable {
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
    val sender: User,
    val text: String,
    val timestamp: Long,
    val magic: Short,
    val seen: Boolean
) : AutoParcelable
