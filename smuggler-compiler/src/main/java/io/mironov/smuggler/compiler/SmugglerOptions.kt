package io.mironov.smuggler.compiler

import java.io.File
import java.util.ArrayList

data class SmugglerOptions(
    val classes: Collection<File>,
    val classpath: Collection<File>,
    val output: File
) {
  class Builder(val output: File) {
    private val classes = ArrayList<File>()
    private val classpath = ArrayList<File>()

    fun classes(files: List<File>) = apply {
      classes.addAll(files)
    }

    fun classpath(files: List<File>) = apply {
      classpath.addAll(files)
    }

    fun build(): SmugglerOptions {
      return SmugglerOptions(classes, classpath, output)
    }
  }
}
