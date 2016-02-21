package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ContentGenerator
import io.mironov.smuggler.compiler.GeneratedContent
import io.mironov.smuggler.compiler.GenerationEnvironment
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.model.DataClassSpec
import io.mironov.smuggler.compiler.model.DataPropertySpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import io.mironov.smuggler.compiler.reflect.Signature
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_BRIDGE
import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Opcodes.ASM5

internal class ParcelableContentGenerator(private val spec: DataClassSpec) : ContentGenerator {
  private companion object {
    private const val ACC_METHOD_DEFAULT = ACC_PUBLIC + ACC_FINAL
    private const val ACC_METHOD_BRIDGE = ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC + ACC_BRIDGE
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(onCreatePatchedDataClass(spec, environment), onCreateCreatorGeneratedContent(spec, environment))
  }

  private fun onCreateCreatorGeneratedContent(spec: DataClassSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(creatorTypeFrom(spec), emptyMap(), environment.newClass {
      val type = creatorTypeFrom(spec)
      val signature = creatorSignatureFrom(spec).toString()
      val interfaces = arrayOf(Types.ANDROID_CREATOR)

      visit(ACC_PUBLIC + ACC_FINAL + ACC_SUPER, type, signature, Types.OBJECT, interfaces)

      newMethod(ACC_PUBLIC, Methods.getConstructor()) {
        // nothing to do
      }

      newMethod(createMethodSpecForCreateFromParcelMethod(spec, false)) {
        newInstance(spec.clazz.type, Methods.getConstructor(spec.properties.map(DataPropertySpec::type))) {
          spec.properties.forEach { property ->
            loadArg(0)
            readValue(TypeAdapterFactory.from(spec, property))
          }
        }
      }

      newMethod(createMethodSpecForNewArrayMethod(spec, false)) {
        loadArg(0)
        newArray(spec.clazz.type)
      }

      newMethod(createMethodSpecForCreateFromParcelMethod(spec, true)) {
        loadThis()
        loadArg(0)
        invokeVirtual(spec.clazz, createMethodSpecForCreateFromParcelMethod(spec, false))
      }

      newMethod(createMethodSpecForNewArrayMethod(spec, true)) {
        loadThis()
        loadArg(0)
        invokeVirtual(spec.clazz, createMethodSpecForNewArrayMethod(spec, false))
      }
    })
  }

  private fun onCreatePatchedDataClass(spec: DataClassSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(spec.clazz.type, emptyMap(), environment.newClass {
      ClassReader(spec.clazz.opener.open()).accept(object : ClassVisitor(ASM5, this) {

      }, ClassReader.SKIP_FRAMES)
    })
  }

  private fun creatorTypeFrom(spec: DataClassSpec): Type {
    return Types.getGeneratedType(spec.clazz.type, "AutoCreator")
  }

  private fun creatorSignatureFrom(spec: DataClassSpec): Signature {
    return Signature.type(Signature.simple(Types.OBJECT), Signature.generic(Types.ANDROID_CREATOR, spec.clazz.type))
  }

  private fun createMethodSpecForNewArrayMethod(spec: DataClassSpec, bridge: Boolean): MethodSpec {
    val flags = if (bridge) ACC_METHOD_BRIDGE else ACC_METHOD_DEFAULT
    val returns = if (bridge) Types.OBJECT else spec.clazz.type
    val method = Type.getMethodType(Types.getArrayType(returns), Types.INT)

    return MethodSpec(flags, "newArray", method)
  }

  private fun createMethodSpecForCreateFromParcelMethod(spec: DataClassSpec, bridge: Boolean): MethodSpec {
    val flags = if (bridge) ACC_METHOD_BRIDGE else ACC_METHOD_DEFAULT
    val returns = if (bridge) Types.OBJECT else spec.clazz.type
    val method = Type.getMethodType(returns, Types.ANDROID_PARCEL)

    return MethodSpec(flags, "createFromParcel", method)
  }

  private fun GeneratorAdapter.writeValue(adapter: TypeAdapter) {
    adapter.writeValue(this)
  }

  private fun GeneratorAdapter.readValue(adapter: TypeAdapter) {
    adapter.readValue(this)
  }
}
