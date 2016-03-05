package io.mironov.smuggler.plugin

import com.android.build.gradle.AppExtension
import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.LibraryExtension
import com.google.common.collect.Iterables
import io.mironov.smuggler.compiler.SmugglerCompiler
import io.mironov.smuggler.compiler.SmugglerOptions
import org.gradle.api.Project

public class SmugglerTransform extends Transform {
  private final Project project

  public SmugglerTransform(final Project project) {
    this.project = project
  }

  @Override
  public void transform(final Context context, final Collection<TransformInput> inputs, final Collection<TransformInput> references, final TransformOutputProvider provider, final boolean incremental) throws IOException, TransformException, InterruptedException {
    final def compiler = new SmugglerCompiler()

    final def input = Iterables.getOnlyElement(Iterables.getOnlyElement(inputs).directoryInputs)
    final def output = provider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)

    final def application = project.extensions.findByType(AppExtension)
    final def library = project.extensions.findByType(LibraryExtension)

    final def libs = new ArrayList<File>()
    final def classes = new ArrayList<File>()

    if (application != null) {
      libs.addAll(application.bootClasspath)
    }

    if (library != null) {
      libs.addAll(library.bootClasspath)
    }

    inputs.each {
      classes.addAll(it.directoryInputs*.file)
      classes.addAll(it.jarInputs*.file)
    }

    references.each {
      libs.addAll(it.directoryInputs*.file)
      libs.addAll(it.jarInputs*.file)
    }

    compiler.compile(new SmugglerOptions.Builder(output)
        .inputs(classes)
        .libs(libs)
        .build()
    )
  }

  @Override
  public Set<QualifiedContent.Scope> getScopes() {
    return EnumSet.of(QualifiedContent.Scope.PROJECT)
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  public Set<QualifiedContent.Scope> getReferencedScopes() {
    return EnumSet.of(
        QualifiedContent.Scope.TESTED_CODE,
        QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        QualifiedContent.Scope.PROVIDED_ONLY
    )
  }

  @Override
  public Map<String, Object> getParameterInputs() {
    return [
        version: BuildConfig.VERSION,
        hash: BuildConfig.GIT_HASH
    ]
  }

  @Override
  public String getName() {
    return "smuggler"
  }

  @Override
  public boolean isIncremental() {
    return false
  }
}