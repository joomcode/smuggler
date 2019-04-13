package com.joom.smuggler.compiler

import com.joom.smuggler.compiler.common.isAutoParcelable
import com.joom.smuggler.compiler.generators.ParcelableContentGenerator
import com.joom.smuggler.compiler.generators.ValueAdapterFactory
import com.joom.smuggler.compiler.model.AutoParcelableClassSpecFactory
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.from
import java.io.File

class SmugglerCompiler(
    private val grip: Grip
) {
  private val environment = GenerationEnvironment(grip)
  private val factory = ValueAdapterFactory.from(grip)

  fun compile(input: File, output: File) {
    val parcelables = grip.select(classes)
        .from(input)
        .where(isAutoParcelable())
        .execute()

    for (parcelable in parcelables.classes) {
      val spec = AutoParcelableClassSpecFactory.from(parcelable)
      val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

      generator.generate(environment).forEach {
        File(output, it.path).writeBytes(it.content)
      }
    }
  }
}
