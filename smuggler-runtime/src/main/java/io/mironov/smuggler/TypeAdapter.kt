package io.mironov.smuggler

import android.os.Parcel
import kotlin.reflect.KClass

@Beta
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class LocalAdapter(vararg val value: KClass<out TypeAdapter<*>>)

@Beta
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GlobalAdapter()

@Beta
interface TypeAdapter<T> {
  fun toParcel(value: T, parcel: Parcel, flags: Int)
  fun fromParcel(parcel: Parcel): T
}
