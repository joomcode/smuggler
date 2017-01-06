package io.mironov.smuggler.compiler

import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.classes
import io.mironov.smuggler.compiler.common.isAutoParcelable
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.generators.ValueAdapterFactory
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpecFactory
import java.io.File
import java.util.ArrayList
import java.util.HashSet

class SmugglerCompiler {
  fun compile(options: SmugglerOptions): SmugglerOutput {
    val inputs = HashSet(options.project + options.subprojects + options.libraries + options.bootclasspath)
    val outputs = ArrayList<File>()

    GripFactory.create(inputs).use { grip ->
      val environment = GenerationEnvironment(grip)
      val factory = ValueAdapterFactory.from(grip, HashSet(options.project + options.subprojects))

      val parcelables = grip.select(classes)
          .from(options.project)
          .where(isAutoParcelable())
          .execute()

      for (parcelable in parcelables.classes) {
        val spec = AutoParcelableClassSpecFactory.from(parcelable)
        val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))
        val content = generator.generate(environment)

        content.forEach {
          File(options.output, it.path).writeBytes(it.content)
        }

        content.forEach {
          outputs.add(File(options.output, it.path))
        }
      }
    }

    return SmugglerOutput(outputs)
  }
}
