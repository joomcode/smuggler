package com.joom.smuggler.plugin

import com.android.build.api.transform.QualifiedContent
import java.io.Serializable
import java.util.EnumSet

data class SmugglerConfiguration(
  val scopes: Set<SmugglerScope>,
  val referencedScopes: Set<SmugglerScope>,
  val verifyNoUnprocessedClasses: Boolean,
) : Serializable

enum class SmugglerScope : Serializable {
  PROJECT,
  SUB_PROJECTS,
  EXTERNAL_LIBRARIES,
  TESTED_CODE,
  PROVIDED_ONLY,
  PROJECT_LOCAL_DEPS,
  SUB_PROJECTS_LOCAL_DEPS,
}

fun SmugglerScope.toTransformScope(): QualifiedContent.Scope {
  return when (this) {
    SmugglerScope.PROJECT -> QualifiedContent.Scope.PROJECT
    SmugglerScope.SUB_PROJECTS -> QualifiedContent.Scope.SUB_PROJECTS
    SmugglerScope.EXTERNAL_LIBRARIES -> QualifiedContent.Scope.EXTERNAL_LIBRARIES
    SmugglerScope.TESTED_CODE -> QualifiedContent.Scope.TESTED_CODE
    SmugglerScope.PROVIDED_ONLY -> QualifiedContent.Scope.PROVIDED_ONLY
    SmugglerScope.PROJECT_LOCAL_DEPS -> QualifiedContent.Scope.PROJECT_LOCAL_DEPS
    SmugglerScope.SUB_PROJECTS_LOCAL_DEPS -> QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS
  }
}

fun Set<SmugglerScope>.toTransformScope(): Set<QualifiedContent.Scope> {
  return mapTo(LinkedHashSet()) { it.toTransformScope() }
}

object SmugglerConfigurationFactory {
  fun createConfigurationForCurrentProject(): SmugglerConfiguration {
    return SmugglerConfiguration(
      scopes = EnumSet.of(
        SmugglerScope.PROJECT
      ),

      referencedScopes = EnumSet.of(
        SmugglerScope.TESTED_CODE,
        SmugglerScope.SUB_PROJECTS,
        SmugglerScope.EXTERNAL_LIBRARIES,
        SmugglerScope.PROVIDED_ONLY
      ),

      verifyNoUnprocessedClasses = true
    )
  }

  fun createConfigurationForCurrentProjectAndSubprojects(): SmugglerConfiguration {
    return SmugglerConfiguration(
      scopes = EnumSet.of(
        SmugglerScope.PROJECT,
        SmugglerScope.SUB_PROJECTS
      ),

      referencedScopes = EnumSet.of(
        SmugglerScope.TESTED_CODE,
        SmugglerScope.EXTERNAL_LIBRARIES,
        SmugglerScope.PROVIDED_ONLY
      ),

      verifyNoUnprocessedClasses = false
    )
  }
}
