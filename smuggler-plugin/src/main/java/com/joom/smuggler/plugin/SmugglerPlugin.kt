package com.joom.smuggler.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

open class SmugglerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val version = GradleVersion.current()
    val minimalVersion = GradleVersion.version("6.8.0")

    if (version < minimalVersion) {
      throw IllegalStateException("Android gradle plugin $version isn't supported anymore. The minimal supported version is $minimalVersion")
    }

    onPrepareExtension(project)
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private fun onPrepareExtension(project: Project) {
    project.extensions.create("smuggler", SmugglerExtension::class.java)
  }

  private fun onPrepareDependencies(project: Project) {
    project.dependencies.add("api", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}")
    project.dependencies.add("androidTestImplementation", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}")
    project.dependencies.add("testImplementation", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}")
  }

  private fun onPrepareTransforms(project: Project) {
    val extension = project.extensions.getByType(SmugglerExtension::class.java)
    val android = findAndroidExtension(project)

    when (computeMode(project)) {
      SmugglerMode.NO_OP -> {
        // nothing to do here
      }

      SmugglerMode.CURRENT_PROJECT_ONLY -> {
        val configuration = SmugglerConfigurationFactory.createConfigurationForCurrentProject()
        val transform = SmugglerTransform(extension, configuration)
        android.registerTransform(transform)
      }

      SmugglerMode.CURRENT_PROJECT_WITH_SUBPROJECTS -> {
        val configuration = SmugglerConfigurationFactory.createConfigurationForCurrentProjectAndSubprojects()
        val transform = SmugglerTransform(extension, configuration)
        android.registerTransform(transform)
      }
    }

    project.afterEvaluate {
      extension.bootClasspath = android.bootClasspath
    }
  }

  private fun findAndroidExtension(project: Project): BaseExtension {
    project.extensions.findByType(AppExtension::class.java)?.let { extension ->
      return extension
    }

    project.extensions.findByType(LibraryExtension::class.java)?.let { extension ->
      return extension
    }

    project.extensions.findByType(TestExtension::class.java)?.let { extension ->
      return extension
    }

    error("'android' or 'android-library' plugin required")
  }

  private fun computeMode(project: Project): SmugglerMode {
    val mode = project.properties["smuggler.plugin.mode"]?.toString()

    if (mode != null) {
      return computeMode(mode)
    }

    return SmugglerMode.CURRENT_PROJECT_ONLY
  }

  private fun computeMode(mode: String?): SmugglerMode {
    val values = SmugglerMode.values()

    values.firstOrNull { it.value == mode }?.let {
      return it
    }

    error("Mode should be one of \"${values.joinToString("|") { it.value }}\", but \"$mode\" was specified.")
  }

  private enum class SmugglerMode(val value: String) {
    CURRENT_PROJECT_ONLY("currentProjectOnly"),
    CURRENT_PROJECT_WITH_SUBPROJECTS("currentProjectWithSubprojects"),
    NO_OP("noOp")
  }
}
