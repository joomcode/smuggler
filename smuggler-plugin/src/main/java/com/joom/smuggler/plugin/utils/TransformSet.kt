/*
 * Copyright 2017 Michael Rozumyanskiy
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

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import java.io.File

class TransformSet private constructor(
    val units: List<TransformUnit>,
    val referencedUnits: List<TransformUnit>,
    val bootClasspath: List<File>
) {

  companion object {
    fun create(invocation: TransformInvocation, bootClasspath: List<File>): TransformSet {
      val units = createTransformUnits(invocation, invocation.inputs)
      val referencedUnits = createTransformUnits(invocation, invocation.referencedInputs)
      return TransformSet(units, referencedUnits, bootClasspath)
    }

    private fun createTransformUnits(
        invocation: TransformInvocation,
        inputs: Collection<TransformInput>
    ): List<TransformUnit> {
      return inputs.flatMap { input ->
        val units = ArrayList<TransformUnit>(input.directoryInputs.size + input.jarInputs.size)
        input.directoryInputs.mapTo(units) { directory ->
          createTransformUnit(invocation, directory, Format.DIRECTORY)
        }
        input.jarInputs.mapTo(units) { jar ->
          createTransformUnit(invocation, jar, Format.JAR)
        }
      }
    }

    private fun createTransformUnit(
        invocation: TransformInvocation,
        input: QualifiedContent,
        format: Format
    ): TransformUnit {
      val output = invocation.outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, format)
      val statusProvider = input.createStatusProvider(invocation.isIncremental)
      return TransformUnit(input.file, output, format.toTransformUnitFormat(), statusProvider)
    }

    private fun Format.toTransformUnitFormat(): TransformUnit.Format {
      return when (this) {
        Format.JAR -> TransformUnit.Format.JAR
        Format.DIRECTORY -> TransformUnit.Format.DIRECTORY
      }
    }

    private fun QualifiedContent.createStatusProvider(incremental: Boolean): Changes {
      return when (this) {
        is DirectoryInput -> Changes.ForDirectory(changedFiles, incremental)
        is JarInput -> Changes.ForJar(status, incremental)
        else -> error("Unknown content $this")
      }
    }
  }
}
