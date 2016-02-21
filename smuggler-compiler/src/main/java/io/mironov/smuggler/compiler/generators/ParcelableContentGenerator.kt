package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ContentGenerator
import io.mironov.smuggler.compiler.GeneratedContent
import io.mironov.smuggler.compiler.GenerationEnvironment
import io.mironov.smuggler.compiler.model.DataClassSpec

internal class ParcelableContentGenerator(private val spec: DataClassSpec) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return emptyList()
  }
}
