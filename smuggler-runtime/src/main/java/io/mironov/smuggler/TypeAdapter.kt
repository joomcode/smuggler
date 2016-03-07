package io.mironov.smuggler

import android.os.Parcel
import kotlin.reflect.KClass

@Beta
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class AdaptedType(val value: KClass<*>)

@Beta
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class LocalAdapter(val value: Array<KClass<out TypeAdapter<*>>>)

@Beta
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GlobalAdapter()

@Beta
interface TypeAdapter<T> {
  fun toParcel(value: T, parcel: Parcel, flags: Int)
  fun fromParcel(parcel: Parcel): T
}
