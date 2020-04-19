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

import com.android.build.api.transform.Status
import java.io.File

interface Changes {
  val status: TransformUnit.Status

  val hasFileStatuses: Boolean
  val files: Collection<File>

  fun getFileStatus(file: File): TransformUnit.Status

  class ForDirectory(
    private val changes: Map<File, Status>,
    private val incremental: Boolean
  ) : Changes {

    override val status get() = if (incremental) TransformUnit.Status.UNKNOWN else TransformUnit.Status.CHANGED

    override val hasFileStatuses get() = incremental
    override val files get() = changes.keys

    override fun getFileStatus(file: File): TransformUnit.Status {
      return changes[file]?.toTransformUnitStatus() ?: TransformUnit.Status.UNKNOWN
    }
  }

  class ForJar(
    private val jarStatus: Status,
    private val incremental: Boolean
  ) : Changes {

    override val status get() = if (incremental) jarStatus.toTransformUnitStatus() else TransformUnit.Status.CHANGED

    override val hasFileStatuses get() = false
    override val files get() = emptyList<File>()

    override fun getFileStatus(file: File): TransformUnit.Status {
      return TransformUnit.Status.UNKNOWN
    }
  }

  companion object {
    private fun Status.toTransformUnitStatus(): TransformUnit.Status {
      return when (this) {
        Status.NOTCHANGED -> TransformUnit.Status.UNCHANGED
        Status.ADDED -> TransformUnit.Status.ADDED
        Status.CHANGED -> TransformUnit.Status.CHANGED
        Status.REMOVED -> TransformUnit.Status.REMOVED
      }
    }
  }
}
