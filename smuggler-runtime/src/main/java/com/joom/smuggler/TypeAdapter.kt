package com.joom.smuggler

import android.os.Parcel
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class LocalAdapter(vararg val value: KClass<out TypeAdapter<*>>)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GlobalAdapter

interface TypeAdapter<T> {
  fun toParcel(value: T, parcel: Parcel, flags: Int)

  fun fromParcel(parcel: Parcel): T
}
