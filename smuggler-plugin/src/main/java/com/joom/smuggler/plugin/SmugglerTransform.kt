package com.joom.smuggler.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.joom.smuggler.compiler.SmugglerCompiler
import com.joom.smuggler.plugin.utils.TransformSet
import com.joom.smuggler.plugin.utils.TransformUnit
import com.joom.smuggler.plugin.utils.copyInputsToOutputs
import com.joom.smuggler.plugin.utils.getClasspath
import java.io.File
import java.util.EnumSet

class SmugglerTransform(
  private val extension: SmugglerExtension,
  private val configuration: SmugglerConfiguration
) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val transformSet = computePreparedTransformSet(invocation)
    val adapters = computeClasspathForAdapters(transformSet, invocation)

    SmugglerCompiler.create(transformSet.getClasspath(), adapters).use { compiler ->
      computeTransformUnitGroups(transformSet)
        .filter { it.containsModifiedUnits() }
        .forEach { transformGroup ->
          if (invocation.isIncremental) {
            compiler.cleanup(transformGroup.output)
          }

          transformGroup.units.forEach { transformUnit ->
            if (transformUnit.changes.status != TransformUnit.Status.REMOVED) {
              compiler.compile(transformUnit.input, transformUnit.output)
            }
          }
        }

      if (configuration.verifyNoUnprocessedClasses) {
        verifyNoUnprocessedClasses(invocation, compiler)
      }
    }
  }

  override fun getScopes(): MutableSet<Scope> {
    return configuration.scopes.toTransformScope().toMutableSet()
  }

  override fun getReferencedScopes(): MutableSet<Scope> {
    return configuration.referencedScopes.toTransformScope().toMutableSet()
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(
      DefaultContentType.CLASSES
    )
  }

  override fun getParameterInputs(): Map<String, Any> {
    return mapOf(
      "cacheable" to extension.cacheable,
      "incremental" to extension.incremental,
      "version" to BuildConfig.VERSION,
      "hash" to BuildConfig.GIT_HASH,
      "bootClasspath" to extension.bootClasspath
        .map { it.absolutePath }
        .sorted()
        .joinToString()
    )
  }

  override fun getName(): String {
    return "smuggler"
  }

  override fun isIncremental(): Boolean {
    return extension.incremental
  }

  override fun isCacheable(): Boolean {
    return extension.cacheable
  }

  private fun verifyNoUnprocessedClasses(invocation: TransformInvocation, compiler: SmugglerCompiler) {
    val referencedFiles = computeClasspathForUnprocessedCandidates(invocation)
    val unprocessedClasses = compiler.findUnprocessedClasses(referencedFiles)

    if (unprocessedClasses.isNotEmpty()) {
      throw IllegalStateException(buildString {
        appendLine("Found ${unprocessedClasses.size} unprocessed AutoParcelable classes.")
        appendLine("Most likely you have a module that transitively depends on Smuggler Runtime, but doesn't apply Smuggler Plugin.")
        appendLine("The full list of unprocessed classes:")

        for (name in unprocessedClasses) {
          appendLine("  - $name")
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

  private fun computeClasspathForAdapters(
    transformSet: TransformSet,
    transformInvocation: TransformInvocation
  ): Collection<File> {
    val classpath = transformSet.getClasspath().toSet()

    val scopes = setOf(Scope.PROJECT, Scope.SUB_PROJECTS)
    val contents = ArrayList<QualifiedContent>()
    val result = ArrayList<File>()

    for (input in transformInvocation.referencedInputs) {
      contents.addAll(input.directoryInputs)
      contents.addAll(input.jarInputs)
    }

    for (input in transformInvocation.inputs) {
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

  private fun computePreparedTransformSet(transformInvocation: TransformInvocation): TransformSet {
    val transformSet = TransformSet.create(transformInvocation, extension.bootClasspath)

    if (!transformInvocation.isIncremental) {
      transformInvocation.outputProvider.deleteAll()
    }

    transformSet.copyInputsToOutputs()
    return transformSet
  }

  private fun computeTransformUnitGroups(transformSet: TransformSet): List<TransformUnitGroup> {
    return transformSet.units
      .groupBy { it.output }
      .map { TransformUnitGroup(it.key, it.value) }
  }

  private fun TransformUnitGroup.containsModifiedUnits(): Boolean {
    return units.any { unit ->
      when (unit.changes.status) {
        TransformUnit.Status.UNKNOWN -> true
        TransformUnit.Status.ADDED -> true
        TransformUnit.Status.CHANGED -> true
        TransformUnit.Status.REMOVED -> true
        TransformUnit.Status.UNCHANGED -> false
      }
    }
  }

  private data class TransformUnitGroup(
    val output: File,
    val units: List<TransformUnit>
  )
}
