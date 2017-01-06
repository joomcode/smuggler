package io.mironov.smuggler.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.google.common.collect.Iterables
import io.mironov.smuggler.compiler.SmugglerCompiler
import io.mironov.smuggler.compiler.SmugglerOptions
import org.gradle.api.Project
import java.io.File
import java.util.ArrayList
import java.util.EnumSet

class SmugglerTransform(private val project: Project) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val options = createOptions(invocation)
    val compiler = createCompiler()

    for (file in options.project) {
      file.copyRecursively(options.output, true)
    }

    compiler.compile(options)
  }

  override fun getScopes(): Set<Scope> {
    return EnumSet.of(Scope.PROJECT)
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(DefaultContentType.CLASSES)
  }

  override fun getReferencedScopes(): Set<Scope> {
    return EnumSet.of(
        Scope.TESTED_CODE,
        Scope.PROJECT_LOCAL_DEPS,
        Scope.SUB_PROJECTS,
        Scope.SUB_PROJECTS_LOCAL_DEPS,
        Scope.EXTERNAL_LIBRARIES
    )
  }

  override fun getParameterInputs(): Map<String, Any> {
    return mapOf(
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

  private fun createCompiler(): SmugglerCompiler {
    return SmugglerCompiler()
  }

  private fun createOptions(invocation: TransformInvocation): SmugglerOptions {
    val input = Iterables.getOnlyElement(Iterables.getOnlyElement(invocation.inputs).directoryInputs)
    val output = invocation.outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)

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
