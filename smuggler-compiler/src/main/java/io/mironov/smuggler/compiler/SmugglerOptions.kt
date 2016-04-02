package io.mironov.smuggler.compiler

import java.io.File
import java.util.ArrayList

data class SmugglerOptions(
    val classes: Collection<File>,
    val bootclasspath: Collection<File>,
    val classpath: Collection<File>,
    val output: File
) {
  class Builder(val output: File) {
    private val classes = ArrayList<File>()
    private val bootclasspath = ArrayList<File>()
    private val classpath = ArrayList<File>()

    fun classes(file: File) = apply {
      classes.add(file)
    }

    fun classes(files: List<File>) = apply {
      classes.addAll(files)
    }

    fun bootclasspath(file: File) = apply {
      bootclasspath.add(file)
    }

    fun bootclasspath(files: List<File>) = apply {
      bootclasspath.addAll(files)
    }

    fun classpath(file: File) = apply {
      classpath.add(file)
    }

    fun classpath(files: List<File>) = apply {
      classpath.addAll(files)
    }

    fun build(): SmugglerOptions {
      return SmugglerOptions(classes, bootclasspath, classpath, output)
    }
  }
}
