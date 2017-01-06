package io.mironov.smuggler.compiler

import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.classes
import io.mironov.smuggler.compiler.common.isAutoParcelable
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.generators.ValueAdapterFactory
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpecFactory
import java.io.File
import java.util.HashSet

class SmugglerCompiler {
  fun compile(options: SmugglerOptions) {
    val grip = GripFactory.create(HashSet(options.project + options.subprojects + options.libraries + options.bootclasspath))

    val environment = GenerationEnvironment(grip)
    val factory = ValueAdapterFactory.from(grip, HashSet(options.project + options.subprojects))

    val parcelables = grip.select(classes)
        .from(options.project)
        .where(isAutoParcelable())
        .execute()

    for (parcelable in parcelables.classes) {
      val spec = AutoParcelableClassSpecFactory.from(parcelable)
      val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

      generator.generate(environment).forEach {
        File(options.output, it.path).writeBytes(it.content)
      }
    }
  }
}
