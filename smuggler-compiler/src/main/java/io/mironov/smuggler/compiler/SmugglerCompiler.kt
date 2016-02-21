package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.model.DataClassSpecFactory
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

class SmugglerCompiler {
  private val logger = LoggerFactory.getLogger(SmugglerCompiler::class.java)

  fun compile(options: SmugglerOptions) {
    val registry = ClassRegistryFactory.create(options)

    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }

    registry.inputs.forEach {
      DataClassSpecFactory.from(it, registry)?.let {
        logger.error("Data class ${it.reference.type.className}, ${it.fields}")
      }
    }
  }
}
