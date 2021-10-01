package com.joom.smuggler.plugin

import com.android.build.api.transform.QualifiedContent
import java.util.EnumSet

data class SmugglerConfiguration(
  val scopes: Set<QualifiedContent.Scope>,
  val referencedScopes: Set<QualifiedContent.Scope>,
  val verifyNoUnprocessedClasses: Boolean
)

object SmugglerConfigurationFactory {
  fun createConfigurationForCurrentProject(): SmugglerConfiguration {
    return SmugglerConfiguration(
      scopes = EnumSet.of(
        QualifiedContent.Scope.PROJECT
      ),

      referencedScopes = EnumSet.of(
        QualifiedContent.Scope.TESTED_CODE,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        QualifiedContent.Scope.PROVIDED_ONLY
      ),

      verifyNoUnprocessedClasses = true
    )
  }

  fun createConfigurationForCurrentProjectAndSubprojects(): SmugglerConfiguration {
    return SmugglerConfiguration(
      scopes = EnumSet.of(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS
      ),

      referencedScopes = EnumSet.of(
        QualifiedContent.Scope.TESTED_CODE,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        QualifiedContent.Scope.PROVIDED_ONLY
      ),

      verifyNoUnprocessedClasses = false
    )
  }
}
