package io.smuggler

import io.smuggler.plugin.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SmugglerPlugin implements Plugin<Project> {
  @Override
  public void apply(final Project project) {
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private static void onPrepareDependencies(final Project project) {
    project.dependencies.add("compile", "io.smuggler:smuggler-runtime:${BuildConfig.VERSION}@aar")
  }

  private static void onPrepareTransforms(final Project project) {
    project.android.registerTransform(new SmugglerTransform(project))
  }
}
