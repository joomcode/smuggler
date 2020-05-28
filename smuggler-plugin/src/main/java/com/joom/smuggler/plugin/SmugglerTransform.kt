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
      val adapters = computeClasspathForAdapters(invocation)
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

    val referencedFiles = computeClasspathForUnprocessedCandidates(invocation)
    val unprocessedClasses = ArrayList<ClassMirror>()

    for (mirror in compiler.findAutoParcelableClasses(referencedFiles)) {
      if (mirror.fields.none { it.name == "CREATOR" }) {
        unprocessedClasses += mirror
      }
    }

    if (unprocessedClasses.isNotEmpty()) {
      throw IllegalStateException(buildString {
        appendln("Found ${unprocessedClasses.size} unprocessed AutoParcelable classes.")
        appendln("Most likely you have a module that transitively depends on Smuggler Runtime, but doesn't apply Smuggler Plugin.")
        appendln("The full list of unprocessed classes:")

        for (mirror in unprocessedClasses) {
          appendln("  - ${mirror.name}")
        }
      })
    }
  }

  private fun computeClasspathForUnprocessedCandidates(invocation: TransformInvocation): Collection<File> {
    val transformSet = TransformSet.create(invocation, emptyList())
    val classpath = transformSet.getClasspath().toSet()

    val scopes = setOf(Scope.SUB_PROJECTS)
    val contents = ArrayList<QualifiedContent>()
    val result = ArrayList<File>()

    for (input in invocation.referencedInputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (content in contents) {
      if (scopes.any { it in content.scopes } && content.file in classpath) {
        result += content.file
      }
    }

    return result
  }

  private fun computeClasspathForAdapters(invocation: TransformInvocation): Collection<File> {
    val transformSet = TransformSet.create(invocation, emptyList())
    val classpath = transformSet.getClasspath().toSet()

    val scopes = setOf(Scope.PROJECT, Scope.SUB_PROJECTS)
    val contents = ArrayList<QualifiedContent>()
    val result = ArrayList<File>()

    for (input in invocation.referencedInputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (input in invocation.inputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (content in contents) {
      if (scopes.any { it in content.scopes } && content.file in classpath) {
        result += content.file
      }
    }

    return result
  }
}
