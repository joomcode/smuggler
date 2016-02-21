package io.mironov.smuggler.compiler

import org.apache.commons.io.FileUtils

class SmugglerCompiler {
  fun compile(options: SmugglerOptions) {
    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }
  }
}
