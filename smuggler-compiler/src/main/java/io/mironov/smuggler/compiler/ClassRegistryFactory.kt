package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.FileOpener
import io.mironov.smuggler.compiler.common.JarOpener
import io.mironov.smuggler.compiler.common.Opener
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.collect
import io.mironov.smuggler.compiler.reflect.ClassReference
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File
import java.util.zip.ZipFile

internal object ClassRegistryFactory {
  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  fun create(options: SmugglerOptions): ClassRegistry {
    return ClassRegistry.Builder()
        .classes(createClassReferences(options.classes))
        .classpath(createClassReferences(options.classpath))
        .build()
  }

  private fun createClassReferences(files: Collection<File>): Collection<ClassReference> {
    return files.collect(mutableListOf()) { result, file ->
      if (file.isFile && file.extension == EXTENSION_JAR) {
        result.addAll(createClassReferencesForJar(file))
      }

      if (file.isDirectory) {
        result.addAll(createClassReferencesForDirectory(file))
      }
    }
  }

  private fun createClassReferencesForJar(file: File): List<ClassReference> {
    return ZipFile(file).use {
      it.entries().asSequence()
          .filter { File(it.name).extension == EXTENSION_CLASS }
          .collect(mutableListOf()) { result, entry ->
            result.add(createClassReference(JarOpener(file, entry.name), it.getInputStream(entry).use {
              it.readBytes()
            }))
          }
    }
  }

  private fun createClassReferencesForDirectory(file: File): List<ClassReference> {
    return file.walk()
        .filter { it.extension == EXTENSION_CLASS }
        .collect(mutableListOf()) { result, file ->
          result.add(createClassReference(FileOpener(file), file.readBytes()))
        }
  }

  private fun createClassReference(opener: Opener, bytes: ByteArray): ClassReference {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    val interfaces = reader.interfaces.orEmpty().map {
      Type.getObjectType(it)
    }

    return ClassReference(reader.access, type, parent, interfaces, opener)
  }
}
