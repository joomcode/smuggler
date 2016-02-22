package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpecFactory
import io.mironov.smuggler.compiler.reflect.ClassReference
import org.apache.commons.io.FileUtils
import java.io.File

class SmugglerCompiler {
  fun compile(options: SmugglerOptions) {
    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)

    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }

    findAutoParcelableClasses(registry).forEach {
      val spec = AutoParcelableClassSpecFactory.from(it, registry)
      val generator = ParcelableContentGenerator(spec)

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
}
