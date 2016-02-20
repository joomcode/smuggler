package io.mironov.smuggler.compiler.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
internal annotation class AnnotationDelegate(val value: String)
