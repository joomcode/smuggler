package io.mironov.smuggler.compiler

import java.io.File
import java.util.ArrayList

data class SmugglerOptions(
    val project: Collection<File>,
    val subprojects: Collection<File>,
    val libraries: Collection<File>,
    val bootclasspath: Collection<File>,
    val output: File
) {
  class Builder(val output: File) {
    private val project = ArrayList<File>()
    private val subprojects = ArrayList<File>()
    private val bootclasspath = ArrayList<File>()
    private val libraries = ArrayList<File>()

    fun project(files: Collection<File>) = apply {
      project.addAll(files)
    }

    fun subprojects(files: Collection<File>) = apply {
      subprojects.addAll(files)
    }

    fun bootclasspath(files: Collection<File>) = apply {
      bootclasspath.addAll(files)
    }

    fun libraries(files: Collection<File>) = apply {
      libraries.addAll(files)
    }

    fun build(): SmugglerOptions {
      return SmugglerOptions(project, subprojects, libraries, bootclasspath, output)
    }
  }
}
