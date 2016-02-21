package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.reflect.ClassReference
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

class SmugglerCompiler {
  private val logger = LoggerFactory.getLogger(SmugglerCompiler::class.java)

  fun compile(options: SmugglerOptions) {
    val registry = ClassRegistryFactory.create(options)

    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }

    findAutoParcelableClasses(registry).forEach {
      logger.error("Data class ${it.type.className}")
    }
  }

  private fun findAutoParcelableClasses(registry: ClassRegistry): Collection<ClassReference> {
    return registry.inputs.filter {
      registry.isSubclassOf(it.type, Types.SMUGGLER_PARCELABLE)
    }
  }
}
