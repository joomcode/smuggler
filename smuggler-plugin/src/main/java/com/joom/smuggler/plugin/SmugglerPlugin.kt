package com.joom.smuggler.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.builder.model.Version
import org.gradle.api.Plugin
import org.gradle.api.Project

open class SmugglerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val version = Version.ANDROID_GRADLE_PLUGIN_VERSION
    val parts = version.split('.', limit = 3)
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0

    if (major < 3) {
      throw IllegalStateException("Android gradle plugin $version isn't supported anymore. The minimal supported version is 3.0.0")
    }

    onPrepareExtension(project)
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private fun onPrepareExtension(project: Project) {
    project.extensions.create("smuggler", SmugglerExtension::class.java)
  }

  private fun onPrepareDependencies(project: Project) {
    project.dependencies.add("api", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
    project.dependencies.add("androidTestImplementation", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
    project.dependencies.add("testImplementation", "com.joom.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
  }

  private fun onPrepareTransforms(project: Project) {
    val extension = project.extensions.getByType(SmugglerExtension::class.java)
    val transform = SmugglerTransform(findAndroidExtension(project), extension)

    if (project.plugins.hasPlugin(LibraryPlugin::class.java)) {
      project.extensions.findByType(LibraryExtension::class.java)!!.registerTransform(transform)
    } else if (project.plugins.hasPlugin(TestPlugin::class.java)) {
      project.extensions.findByType(TestExtension::class.java)!!.registerTransform(transform)
    } else if (project.plugins.hasPlugin(AppPlugin::class.java)) {
      project.extensions.findByType(AppExtension::class.java)!!.registerTransform(transform)
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
}
