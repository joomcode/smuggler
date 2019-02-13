package com.joom.smuggler.compiler

internal interface ContentGenerator {
  fun generate(environment: GenerationEnvironment): Collection<GeneratedContent>
}
