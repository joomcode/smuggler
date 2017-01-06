package io.mironov.smuggler.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
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

class SmugglerTransform(val project: Project) : Transform() {
  override fun transform(invocation: TransformInvocation) {
    val compiler = SmugglerCompiler()

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
      projects.addAll(it.directoryInputs.map(QualifiedContent::getFile))
      projects.addAll(it.jarInputs.map(QualifiedContent::getFile))
    }

    invocation.referencedInputs.forEach {
      libraries.addAll(it.jarInputs
          .filter { !it.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS) }
          .map { it.file }
      )

      libraries.addAll(it.directoryInputs
          .filter { !it.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS) }
          .map { it.file }
      )

      subprojects.addAll(it.jarInputs
          .filter { it.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS) }
          .map { it.file }
      )

      subprojects.addAll(it.directoryInputs
          .filter { it.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS) }
          .map { it.file }
      )
    }

    compiler.compile(SmugglerOptions.Builder(output)
        .project(projects)
        .subprojects(subprojects)
        .bootclasspath(bootclasspath)
        .libraries(libraries)
        .build()
    )
  }

  override fun getScopes(): Set<QualifiedContent.Scope> {
    return EnumSet.of(QualifiedContent.Scope.PROJECT)
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  override fun getReferencedScopes(): Set<QualifiedContent.Scope> {
    return EnumSet.of(
        QualifiedContent.Scope.TESTED_CODE,
        QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES
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
}
