package io.mironov.smuggler.compiler

import org.objectweb.asm.Type
import java.text.MessageFormat

open class SmugglerException : RuntimeException {
  constructor(message: String, vararg args: Any?) : super(MessageFormat.format(message, *args))
}

open class InvalidAutoParcelableException : SmugglerException {
  constructor(type: Type, message: String, vararg args: Any?) : super("Invalid AutoParcelable class '${type.className}'. $message", *args)
}

