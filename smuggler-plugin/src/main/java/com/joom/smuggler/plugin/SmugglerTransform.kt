package com.joom.smuggler.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.joom.smuggler.compiler.SmugglerCompiler
import com.joom.smuggler.plugin.utils.TransformSet
import com.joom.smuggler.plugin.utils.TransformUnit
import com.joom.smuggler.plugin.utils.copyInputsToOutputs
import com.joom.smuggler.plugin.utils.getClasspath
import io.michaelrocks.grip.GripFactory
import org.gradle.api.Project
import java.io.File
import java.util.ArrayList
import java.util.EnumSet

class SmugglerTransform(
    private val project: Project,
    private val extension: SmugglerExtension
) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val transformSet = TransformSet.create(invocation, createBootClasspath())

    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    transformSet.copyInputsToOutputs()

    GripFactory.create(transformSet.getClasspath()).use { grip ->
      val compiler = SmugglerCompiler(grip)

      for (unit in transformSet.units) {
        compiler.cleanup(unit.output)
      }

      for (unit in transformSet.units) {
        if (unit.changes.status != TransformUnit.Status.REMOVED) {
          compiler.compile(unit.input, unit.output)
        }
      }
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

  private fun createBootClasspath(): List<File> {
    val result = ArrayList<File>()

    val application = project.extensions.findByType(AppExtension::class.java)
    val library = project.extensions.findByType(LibraryExtension::class.java)

    if (application != null && application.bootClasspath != null) {
      result.addAll(application.bootClasspath)
    }

    if (library != null && library.bootClasspath != null) {
      result.addAll(library.bootClasspath)
    }

    return result
  }
}
