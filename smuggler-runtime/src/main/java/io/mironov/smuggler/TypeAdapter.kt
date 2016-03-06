package io.mironov.smuggler

import android.os.Parcel

@Beta
interface TypeAdapter<T> {
  fun toParcel(value: T, parcel: Parcel, flags: Int)
  fun fromParcel(parcel: Parcel): T
}
