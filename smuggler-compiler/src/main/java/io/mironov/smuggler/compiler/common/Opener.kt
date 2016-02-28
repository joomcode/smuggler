package io.mironov.smuggler.compiler.common

import java.io.File
import java.util.zip.ZipFile

internal interface Opener {
  fun open(): ByteArray
}

internal class FileOpener(private val file: File) : Opener {
  override fun open(): ByteArray {
    return file.readBytes()
  }
}

internal class JarOpener(private val file: File, private val entry: String) : Opener {
  override fun open(): ByteArray {
    return ZipFile(file).use {
      it.getInputStream(it.getEntry(entry)).use {
        it.readBytes()
      }
    }
  }
}
