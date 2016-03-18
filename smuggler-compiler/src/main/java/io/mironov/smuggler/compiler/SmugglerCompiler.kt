package io.mironov.smuggler.compiler

import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.and
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.isInterface
import io.michaelrocks.grip.not
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.isSubclass
import io.mironov.smuggler.compiler.generators.ParcelableContentGenerator
import io.mironov.smuggler.compiler.generators.ValueAdapterFactory
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpecFactory
import java.io.File
import java.util.HashSet

class SmugglerCompiler {
  fun compile(options: SmugglerOptions) {
    val files = HashSet(options.classes + options.classpath)
    val grip = GripFactory.create(files)

    val environment = GenerationEnvironment(grip)
    val factory = ValueAdapterFactory.from(grip, files)

    val parcelables = grip.select(classes)
        .from(options.classes.toList())
        .where(not(isInterface()) and isSubclass(Types.SMUGGLER_PARCELABLE, grip))

    options.classes.forEach {
      it.copyRecursively(options.output, true)
    }

    parcelables.execute().classes.forEach {
      val spec = AutoParcelableClassSpecFactory.from(it)
      val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

      generator.generate(environment).forEach {
        File(options.output, it.path).writeBytes(it.content)
      }
    }
  }
}
