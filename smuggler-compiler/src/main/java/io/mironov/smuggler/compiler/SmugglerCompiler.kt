package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.isInterface
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.generators.ValueAdapterFactory
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpecFactory
import io.mironov.smuggler.compiler.reflect.ClassReference
import java.io.File

class SmugglerCompiler {
  fun compile(options: SmugglerOptions) {
    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)
    val factory = ValueAdapterFactory.from(registry)

    options.inputs.forEach {
      it.copyRecursively(options.output, true)
    }

    findAutoParcelableClasses(registry).forEach {
      val spec = AutoParcelableClassSpecFactory.from(it, registry)
      val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

      generator.generate(environment).forEach {
        File(options.output, it.path).writeBytes(it.content)
      }
    }
  }

  private fun findAutoParcelableClasses(registry: ClassRegistry): Collection<ClassReference> {
    return registry.inputs.filter {
      !it.isInterface && registry.isSubclassOf(it.type, Types.SMUGGLER_PARCELABLE)
    }
  }
}
