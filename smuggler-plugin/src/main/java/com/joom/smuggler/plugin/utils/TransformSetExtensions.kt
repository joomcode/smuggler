/*
 * Copyright 2019 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joom.smuggler.plugin.utils

import java.io.File

fun TransformSet.copyInputsToOutputs() {
  units.forEach { unit ->
    when (unit.format) {
      TransformUnit.Format.DIRECTORY -> unit.input.copyDirectoryTo(unit.output, unit.changes)
      TransformUnit.Format.JAR -> unit.input.copyJarTo(unit.output, unit.changes)
    }
  }
}

fun TransformSet.getClasspath(): List<File> {
  val classpath = ArrayList<File>(units.size + referencedUnits.size + bootClasspath.size)

  for (unit in units) {
    if (unit.changes.status != TransformUnit.Status.REMOVED) {
      classpath += unit.input
    }
  }

  for (unit in referencedUnits) {
    if (unit.changes.status != TransformUnit.Status.REMOVED) {
      classpath += unit.input
    }
  }

  classpath += bootClasspath
  return classpath
}

private fun File.copyDirectoryTo(target: File, changes: Changes) {
  if (!changes.hasFileStatuses) {
    target.deleteRecursively()
    if (exists()) {
      copyRecursively(target)
    }
    return
  }

  target.mkdirs()

  changes.files.forEach { file ->
    val status = changes.getFileStatus(file)
    val relativePath = file.toRelativeString(this)
    val targetFile = File(target, relativePath)

    file.applyChangesTo(targetFile, status)
  }
}

private fun File.copyJarTo(target: File, changes: Changes) {
  applyChangesTo(target, changes.status)
}

private fun File.applyChangesTo(target: File, status: TransformUnit.Status) {
  when (status) {
    TransformUnit.Status.UNCHANGED -> {
      // nothing to do
    }

    TransformUnit.Status.REMOVED -> {
      target.deleteRecursively()
    }

    TransformUnit.Status.ADDED -> {
      target.deleteRecursively()
      copyRecursively(target, true)
    }

    TransformUnit.Status.CHANGED -> {
      target.deleteRecursively()
      copyRecursively(target, true)
    }

    TransformUnit.Status.UNKNOWN -> {
      applyChangesTo(target, if (exists()) TransformUnit.Status.CHANGED else TransformUnit.Status.REMOVED)
    }
  }
}
