package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.model.DataClassSpec
import io.mironov.smuggler.compiler.model.DataClassSpecFactory
import io.mironov.smuggler.compiler.reflect.ClassReference
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File

class SmugglerCompiler {
  private val logger = LoggerFactory.getLogger(SmugglerCompiler::class.java)

  fun compile(options: SmugglerOptions) {
    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)

    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }

    findAutoParcelableClasses(registry).forEach {
      val spec = findDataClassSpecFromAutoParcelable(it, registry)
      val generator = createContentGeneratorFrom(spec)

      generator.generate(environment).forEach {
        FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
      }
    }
  }

  private fun findAutoParcelableClasses(registry: ClassRegistry): Collection<ClassReference> {
    return registry.inputs.filter {
      registry.isSubclassOf(it.type, Types.SMUGGLER_PARCELABLE)
    }
  }

  private fun findDataClassSpecFromAutoParcelable(reference: ClassReference, registry: ClassRegistry): DataClassSpec {
    return DataClassSpecFactory.from(reference, registry)
        ?: throw SmugglerException("Invalid AutoParcelable class ''{0}'', only data classes can implement AutoParcelable interface.", reference.type.className)
  }

  private fun createContentGeneratorFrom(spec: DataClassSpec): ContentGenerator {
    return ParcelableContentGenerator(spec)
  }
}
