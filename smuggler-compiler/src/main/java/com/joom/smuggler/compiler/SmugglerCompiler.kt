package com.joom.smuggler.compiler

import com.joom.smuggler.compiler.common.isAutoParcelable
import com.joom.smuggler.compiler.generators.ParcelableContentGenerator
import com.joom.smuggler.compiler.generators.ValueAdapterFactory
import com.joom.smuggler.compiler.model.AutoParcelableClassSpecFactory
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.mirrors.ClassMirror
import java.io.Closeable
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.io.path.writeBytes

class SmugglerCompiler private constructor(
  private val grip: Grip,
  private val adapters: Collection<File>
) : Closeable {
  private val environment = GenerationEnvironment(grip)
  private val factory = ValueAdapterFactory.from(grip, adapters)

  fun compile(input: File, output: File) {
    createSink(output).use { sink ->
      findAutoParcelableClasses(listOf(input)).forEach { classMirror ->
        val spec = AutoParcelableClassSpecFactory.from(classMirror)
        val generator = ParcelableContentGenerator(spec, ValueAdapterFactory.from(factory, spec))

        generator.generate(environment).forEach { content ->
          sink.write(content.path, content.content)
        }
      }
    }
  }

  fun findUnprocessedClasses(referencedFiles: Collection<File>): List<String> {
    return ArrayList<String>().also { unprocessedClasses ->
      for (mirror in findAutoParcelableClasses(referencedFiles)) {
        if (mirror.fields.none { it.name == "CREATOR" }) {
          unprocessedClasses += mirror.name
        }
      }
    }
  }

  fun cleanup(output: File) {
    if (output.exists()) {
      createJanitor(output).cleanup()
    }
  }

  override fun close() {
    grip.close()
  }

  private fun findAutoParcelableClasses(sources: Iterable<File>): Iterable<ClassMirror> {
    return grip.select(classes)
      .from(sources)
      .where(isAutoParcelable())
      .execute()
      .values
  }

  private fun createSink(file: File): Sink {
    return if (file.isDirectory) {
      DirectorySink(file)
    } else {
      JarSink(file)
    }
  }

  private fun createJanitor(file: File): Janitor {
    return if (file.isDirectory) {
      DirectoryJanitor(file)
    } else {
      JarJanitor(file)
    }
  }

  private interface Sink : Closeable {
    fun write(path: String, content: ByteArray)
  }

  private interface Janitor {
    fun cleanup()
  }

  private class DirectoryJanitor(
    private val directory: File
  ) : Janitor {
    override fun cleanup() {
      directory.walkTopDown().forEach { file ->
        if (file.isFile && file.endsWith("\$\$AutoCreator.class")) {
          file.delete()
        }
      }
    }
  }

  private class JarJanitor(
    private val file: File
  ) : Janitor {
    override fun cleanup() {
      val entries = ZipFile(file).use { zipFile ->
        zipFile.entries().toList().map { zipEntry ->
          zipEntry.name
        }
      }

      FileSystems.newFileSystem(file.toPath(), null).use { fileSystem ->
        entries.forEach { entry ->
          if (entry.endsWith("\$\$AutoCreator.class")) {
            Files.delete(fileSystem.getPath(entry))
          }
        }
      }
    }
  }

  private class DirectorySink(
    private val directory: File
  ) : Sink {
    override fun write(path: String, content: ByteArray) {
      File(directory, path).writeBytes(content)
    }

    override fun close() {
      // nothing to do
    }
  }

  private class JarSink(
    private val file: File
  ) : Sink {
    private val fileSystem = FileSystems.newFileSystem(file.toPath(), null)

    override fun write(path: String, content: ByteArray) {
      fileSystem.getPath(path).writeBytes(content)
    }

    override fun close() {
      fileSystem.close()
    }
  }

  companion object {
    fun create(transformClasspath: Collection<File>, adapters: Collection<File>): SmugglerCompiler {
      val grip = GripFactory.create(transformClasspath)
      return SmugglerCompiler(grip, adapters)
    }
  }
}
