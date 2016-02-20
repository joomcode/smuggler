package io.mironov.smuggler.compiler

import java.text.MessageFormat

class SmugglerException : RuntimeException {
  constructor(message: String, vararg args: Any?) : super(MessageFormat.format(message, *args))
}
