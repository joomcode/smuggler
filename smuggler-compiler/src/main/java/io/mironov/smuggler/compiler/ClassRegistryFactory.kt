package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.FileOpener
import io.mironov.smuggler.compiler.common.JarOpener
import io.mironov.smuggler.compiler.common.Opener
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.reflect.ClassReference
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList
import java.util.zip.ZipFile

internal object ClassRegistryFactory {
  private val logger = LoggerFactory.getLogger(ClassRegistryFactory::class.java)

  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  fun create(options: SmugglerOptions): ClassRegistry {
    return ClassRegistry.Builder()
        .inputs(createClassReferences(options.inputs))
        .references(createClassReferences(options.libs))
        .build()
  }

  private fun createClassReferences(files: Collection<File>): Collection<ClassReference> {
    return ArrayList<ClassReference>().apply {
      for (file in files) {
        logger.info("Generating class references for {}", file.absolutePath)

        if (file.isFile && FilenameUtils.getExtension(file.absolutePath) == EXTENSION_JAR) {
          addAll(createClassReferencesForJar(file))
        }

        if (file.isDirectory) {
          addAll(createClassReferencesForDirectory(file))
        }
      }
    }
  }

  private fun createClassReferencesForJar(file: File): List<ClassReference> {
    return ArrayList<ClassReference>().apply {
      ZipFile(file).use {
        for (entry in it.entries()) {
          if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
            add(createClassReference(JarOpener(file, entry.name), it.getInputStream(entry).use {
              IOUtils.toByteArray(it)
            }))
          }
        }
      }
    }
  }

  private fun createClassReferencesForDirectory(file: File): List<ClassReference> {
    return ArrayList<ClassReference>().apply {
      FileUtils.iterateFiles(file, arrayOf(EXTENSION_CLASS), true).forEach {
        add(createClassReference(FileOpener(it), FileUtils.readFileToByteArray(it)))
      }
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
