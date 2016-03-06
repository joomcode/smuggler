package io.mironov.smuggler

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ClassAdapter(val value: Array<KClass<out TypeAdapter<*>>>)
