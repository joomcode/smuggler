package io.mironov.smuggler.compiler

import io.mironov.smuggler.compiler.common.FileOpener
import io.mironov.smuggler.compiler.common.JarOpener
import io.mironov.smuggler.compiler.common.Opener
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.reflect.ClassReference
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

        if (file.isFile && file.extension == EXTENSION_JAR) {
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
        for (entry in it.entries().asSequence().filter { File(it.name).extension == EXTENSION_CLASS }) {
          add(createClassReference(JarOpener(file, entry.name), it.getInputStream(entry).use {
            it.readBytes()
          }))
        }
      }
    }
  }

  private fun createClassReferencesForDirectory(file: File): List<ClassReference> {
    return ArrayList<ClassReference>().apply {
      file.walk().filter { it.extension == EXTENSION_CLASS }.forEach {
        add(createClassReference(FileOpener(it), it.readBytes()))
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
