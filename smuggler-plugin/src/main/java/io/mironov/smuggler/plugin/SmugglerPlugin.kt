package io.mironov.smuggler.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class SmugglerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private fun onPrepareDependencies(project: Project) {
    project.dependencies.add("compile", "io.mironov.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
    project.dependencies.add("androidTestCompile", "io.mironov.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
    project.dependencies.add("testCompile", "io.mironov.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
  }

  private fun onPrepareTransforms(project: Project) {
    if (project.plugins.hasPlugin(LibraryPlugin::class.java)) {
      project.extensions.findByType(LibraryExtension::class.java).registerTransform(SmugglerTransform(project))
    } else if (project.plugins.hasPlugin(TestPlugin::class.java)) {
      project.extensions.findByType(TestExtension::class.java).registerTransform(SmugglerTransform(project))
    } else if (project.plugins.hasPlugin(AppPlugin::class.java)) {
      project.extensions.findByType(AppExtension::class.java).registerTransform(SmugglerTransform(project))
    }
  }
}
