package com.joom.smuggler.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.joom.smuggler.compiler.SmugglerCompiler
import com.joom.smuggler.compiler.SmugglerOptions
import org.gradle.api.Project
import java.io.File
import java.util.ArrayList
import java.util.EnumSet

class SmugglerTransform(
    private val project: Project,
    private val extension: SmugglerExtension
) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val options = createOptions(invocation)
    val compiler = SmugglerCompiler()

    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    for (file in options.project) {
      file.copyRecursively(options.output, true)
    }

    compiler.compile(options)
  }

  override fun getScopes(): MutableSet<Scope> {
    return EnumSet.of(Scope.PROJECT)
  }

  override fun getReferencedScopes(): MutableSet<Scope> {
    return EnumSet.of(
        Scope.TESTED_CODE,
        Scope.SUB_PROJECTS,
        Scope.EXTERNAL_LIBRARIES
    )
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(DefaultContentType.CLASSES)
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
    return false
  }

  private fun createOptions(invocation: TransformInvocation): SmugglerOptions {
    val inputs = invocation.inputs.flatMap { it.directoryInputs }
    val types = inputs.flatMapTo(LinkedHashSet()) { it.contentTypes }
    val scopes = inputs.flatMapTo(LinkedHashSet()) { it.scopes }
    val output = invocation.outputProvider.getContentLocation("classes", types, scopes, Format.DIRECTORY)

    val application = project.extensions.findByType(AppExtension::class.java)
    val library = project.extensions.findByType(LibraryExtension::class.java)

    val projects = ArrayList<File>()
    val subprojects = ArrayList<File>()
    val bootclasspath = ArrayList<File>()
    val libraries = ArrayList<File>()

    if (application != null && application.bootClasspath != null) {
      bootclasspath.addAll(application.bootClasspath)
    }

    if (library != null && library.bootClasspath != null) {
      bootclasspath.addAll(library.bootClasspath)
    }

    invocation.inputs.forEach {
      collect(it, projects)
    }

    invocation.referencedInputs.forEach {
      collect(it, subprojects) {
        it.scopes.contains(Scope.SUB_PROJECTS)
      }

      collect(it, libraries) {
        !it.scopes.contains(Scope.SUB_PROJECTS)
      }
    }

    return SmugglerOptions.Builder(output)
        .project(projects)
        .subprojects(subprojects)
        .bootclasspath(bootclasspath)
        .libraries(libraries)
        .build()
  }

  private fun collect(input: TransformInput, output: MutableList<File>, filter: (QualifiedContent) -> Boolean = { true }) {
    for (directory in input.directoryInputs) {
      if (filter(directory)) {
        output.add(directory.file)
      }
    }

    for (jar in input.jarInputs) {
      if (filter(jar)) {
        output.add(jar.file)
      }
    }
  }
}
