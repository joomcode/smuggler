package com.joom.smuggler.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.joom.smuggler.compiler.SmugglerCompiler
import com.joom.smuggler.plugin.utils.TransformSet
import com.joom.smuggler.plugin.utils.TransformUnit
import com.joom.smuggler.plugin.utils.copyInputsToOutputs
import com.joom.smuggler.plugin.utils.getClasspath
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.mirrors.ClassMirror
import java.io.File
import java.util.EnumSet

class SmugglerTransform(
  private val android: BaseExtension,
  private val extension: SmugglerExtension
) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val transformSet = TransformSet.create(invocation, android.bootClasspath)
    val transformClasspath = transformSet.getClasspath()

    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    transformSet.copyInputsToOutputs()

    GripFactory.create(transformClasspath).use { grip ->
      val adapters = computeTypeAdapterSources(invocation)
      val compiler = SmugglerCompiler(grip, adapters)

      for (unit in transformSet.units) {
        compiler.cleanup(unit.output)
      }

      for (unit in transformSet.units) {
        if (unit.changes.status != TransformUnit.Status.REMOVED) {
          compiler.compile(unit.input, unit.output)
        }
      }

      verifyNoUnprocessedClasses(invocation, compiler)
    }
  }

  override fun getScopes(): MutableSet<Scope> {
    return EnumSet.of(
      Scope.PROJECT
    )
  }

  override fun getReferencedScopes(): MutableSet<Scope> {
    return EnumSet.of(
      Scope.TESTED_CODE,
      Scope.SUB_PROJECTS,
      Scope.EXTERNAL_LIBRARIES
    )
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(
      DefaultContentType.CLASSES
    )
  }

  override fun getParameterInputs(): Map<String, Any> {
    return mapOf(
      "incremental" to extension.incremental,
      "version" to BuildConfig.VERSION,
      "hash" to BuildConfig.GIT_HASH
    )
  }

  override fun getName(): String {
    return "smuggler"
  }

  override fun isIncremental(): Boolean {
    return true
  }

  override fun isCacheable(): Boolean {
    return true
  }

  private fun verifyNoUnprocessedClasses(invocation: TransformInvocation, compiler: SmugglerCompiler) {
    if (android !is AppExtension) {
      return
    }

    val referencedContents = ArrayList<QualifiedContent>()
    val referencedFiles = ArrayList<File>()
    val unprocessed = ArrayList<ClassMirror>()

    for (referencedInput in invocation.referencedInputs) {
      referencedContents.addAll(referencedInput.directoryInputs)
      referencedContents.addAll(referencedInput.jarInputs)
    }

    for (referencedContent in referencedContents) {
      if (Scope.SUB_PROJECTS in referencedContent.scopes) {
        referencedFiles += referencedContent.file
      }
    }

    for (mirror in compiler.findAutoParcelableClasses(referencedFiles)) {
      if (mirror.fields.none { it.name == "CREATOR" }) {
        unprocessed += mirror
      }
    }

    if (unprocessed.isNotEmpty()) {
      throw IllegalStateException(buildString {
        appendln("Found ${unprocessed.size} unprocessed AutoParcelable classes.")
        appendln("Most likely you have a module that transitively depends on Smuggler Runtime, but doesn't apply Smuggler Plugin.")
        appendln("The full list of unprocessed classes:")

        for (mirror in unprocessed) {
          appendln("  - ${mirror.name}")
        }
      })
    }
  }

  private fun computeTypeAdapterSources(invocation: TransformInvocation): Collection<File> {
    val result = ArrayList<File>()
    val contents = ArrayList<QualifiedContent>()

    for (input in invocation.inputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (input in invocation.referencedInputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (content in contents) {
      if (Scope.PROJECT in content.scopes || Scope.SUB_PROJECTS in content.scopes) {
        result += content.file
      }
    }

    return result
  }
}
