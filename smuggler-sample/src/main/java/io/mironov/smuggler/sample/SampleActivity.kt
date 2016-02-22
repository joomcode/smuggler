package io.mironov.smuggler.sample

import android.app.Activity
import android.os.Bundle
import io.mironov.smuggler.AutoParcelable

class SampleActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sample)
  }
}

data class User(
    val firstName: String,
    val lastName: String
) : AutoParcelable

data class Message(
    val id: Int,
    val sender: User,
    val text: String,
    val timestamp: Long,
    val magic: Short,
    val seen: Boolean,
    val extras: Bundle,

    val booleans: BooleanArray,
    val chars: CharArray,
    val doubles: DoubleArray,
    val floats: FloatArray,
    val ints: IntArray,
    val logns: LongArray,
    val strings: Array<String>
) : AutoParcelable
