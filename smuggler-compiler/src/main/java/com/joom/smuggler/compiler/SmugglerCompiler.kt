package com.joom.smuggler.compiler

import com.joom.smuggler.compiler.common.isAutoParcelable
import com.joom.smuggler.compiler.generators.ParcelableContentGenerator
import com.joom.smuggler.compiler.generators.ValueAdapterFactory
import com.joom.smuggler.compiler.model.AutoParcelableClassSpecFactory
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.mirrors.ClassMirror
import java.io.File

class SmugglerCompiler(
  private val grip: Grip,
  private val adapters: Collection<File>
) {
  private val environment = GenerationEnvironment(grip)
  private val factory = ValueAdapterFactory.from(grip, adapters)

  fun compile(input: File, output: File) {
    for (parcelable in findAutoParcelableClasses(listOf(input))) {
      val spec = AutoParcelableClassSpecFactory.from(parcelable)
      val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

      generator.generate(environment).forEach {
        File(output, it.path).writeBytes(it.content)
      }
    }
  }

  fun findAutoParcelableClasses(sources: Iterable<File>): Iterable<ClassMirror> {
    return grip.select(classes)
      .from(sources)
      .where(isAutoParcelable())
      .execute()
      .values
  }

  fun cleanup(output: File) {
    output.walkTopDown().forEach { file ->
      if (file.isFile && file.endsWith("\$\$AutoCreator.class")) {
        file.delete()
      }
    }
  }
}
