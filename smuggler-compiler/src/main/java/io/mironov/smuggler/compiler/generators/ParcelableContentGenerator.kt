package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ContentGenerator
import io.mironov.smuggler.compiler.GeneratedContent
import io.mironov.smuggler.compiler.GenerationEnvironment
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.given
import io.mironov.smuggler.compiler.common.isStatic
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpec
import io.mironov.smuggler.compiler.model.AutoParcelablePropertySpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import io.mironov.smuggler.compiler.reflect.Signature
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_BRIDGE
import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.commons.Method

internal class ParcelableContentGenerator(private val spec: AutoParcelableClassSpec) : ContentGenerator {
  private companion object {
    private const val ACC_METHOD_DEFAULT = ACC_PUBLIC + ACC_FINAL
    private const val ACC_METHOD_BRIDGE = ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC + ACC_BRIDGE
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(onCreatePatchedDataClass(spec, environment), onCreateCreatorGeneratedContent(spec, environment))
  }

  private fun onCreateCreatorGeneratedContent(spec: AutoParcelableClassSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(creatorTypeFrom(spec), emptyMap(), environment.newClass {
      val type = creatorTypeFrom(spec)
      val signature = creatorTypeSignatureFrom(spec)
      val interfaces = arrayOf(Types.ANDROID_CREATOR)

      visit(ACC_PUBLIC + ACC_FINAL + ACC_SUPER, type, signature, Types.OBJECT, interfaces)

      newMethod(ACC_PUBLIC, Methods.getConstructor()) {
        loadThis()
        invokeConstructor(Types.OBJECT, Methods.getConstructor())
      }

      newMethod(createMethodSpecForCreateFromParcelMethod(spec, false)) {
        newInstance(spec.clazz.type, Methods.getConstructor(spec.properties.map(AutoParcelablePropertySpec::type))) {
          spec.properties.forEach {
            TypeAdapterFactory.from(environment.registry, spec, it).readValue(this, spec, it)
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
        invokeVirtual(type, createMethodSpecForCreateFromParcelMethod(spec, false))
        checkCast(Types.OBJECT)
      }

      newMethod(createMethodSpecForNewArrayMethod(spec, true)) {
        loadThis()
        loadArg(0)
        invokeVirtual(type, createMethodSpecForNewArrayMethod(spec, false))
        checkCast(Types.getArrayType(Types.OBJECT))
      }
    })
  }

  private fun onCreatePatchedDataClass(spec: AutoParcelableClassSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(spec.clazz.type, emptyMap(), environment.newClass {
      ClassReader(spec.clazz.opener.open()).accept(object : ClassVisitor(ASM5, this) {
        override fun visit(version: Int, access: Int, name: String, signature: String?, parent: String?, exceptions: Array<out String>?) {
          super.visit(version, access, name, signature, parent, exceptions)
          visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, "CREATOR", creatorTypeFrom(spec), creatorFieldSignatureFrom(spec))
        }

        override fun visitField(access: Int, name: String, description: String, signature: String?, value: Any?): FieldVisitor? {
          return given(!shouldExcludeFieldFromParcelableClass(access, name, description, signature)) {
            createInterceptedFieldVisitor(super.visitField(access, name, description, signature, value), access, name, description, signature, value)
          }
        }

        override fun visitMethod(access: Int, name: String, description: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
          return given(!shouldExcludeMethodFromParcelableClass(access, name, description, signature)) {
            createInterceptedMethodVisitor(super.visitMethod(access, name, description, signature, exceptions), access, name, description, signature, exceptions)
          }
        }
      }, ClassReader.SKIP_FRAMES)

      if (spec.clazz.getDeclaredMethod("<clinit>", Types.VOID) == null) {
        newMethod(ACC_PUBLIC + ACC_STATIC, Methods.getStaticConstructor()) {
          newInstance(creatorTypeFrom(spec), Methods.getConstructor())
          putStatic(spec.clazz.type, "CREATOR", creatorTypeFrom(spec))
        }
      }

      newMethod(createMethodSpecForWriteToParcelMethod(spec)) {
        spec.properties.forEach {
          TypeAdapterFactory.from(environment.registry, spec, it).writeValue(this, spec, it)
        }
      }

      newMethod(createMethodSpecForDescribeContentsMethod(spec)) {
        push(0)
      }
    })
  }

  private fun creatorTypeFrom(spec: AutoParcelableClassSpec): Type {
    return Types.getGeneratedType(spec.clazz.type, "AutoCreator")
  }

  private fun creatorTypeSignatureFrom(spec: AutoParcelableClassSpec): String {
    return Signature.type(Signature.simple(Types.OBJECT), Signature.generic(Types.ANDROID_CREATOR, spec.clazz.type)).toString()
  }

  private fun creatorFieldSignatureFrom(spec: AutoParcelableClassSpec): String {
    return Signature.generic(Types.ANDROID_CREATOR, spec.clazz.type).toString()
  }

  private fun createMethodSpecForNewArrayMethod(spec: AutoParcelableClassSpec, bridge: Boolean): MethodSpec {
    val flags = if (bridge) ACC_METHOD_BRIDGE else ACC_METHOD_DEFAULT
    val returns = if (bridge) Types.OBJECT else spec.clazz.type
    val method = Type.getMethodType(Types.getArrayType(returns), Types.INT)

    return MethodSpec(flags, "newArray", method)
  }

  private fun createMethodSpecForCreateFromParcelMethod(spec: AutoParcelableClassSpec, bridge: Boolean): MethodSpec {
    val flags = if (bridge) ACC_METHOD_BRIDGE else ACC_METHOD_DEFAULT
    val returns = if (bridge) Types.OBJECT else spec.clazz.type
    val method = Type.getMethodType(returns, Types.ANDROID_PARCEL)

    return MethodSpec(flags, "createFromParcel", method)
  }

  private fun createMethodSpecForDescribeContentsMethod(spec: AutoParcelableClassSpec): MethodSpec {
    return MethodSpec(ACC_METHOD_DEFAULT, "describeContents", Type.getMethodType(Types.INT))
  }

  private fun createMethodSpecForWriteToParcelMethod(spec: AutoParcelableClassSpec): MethodSpec {
    return MethodSpec(ACC_METHOD_DEFAULT, "writeToParcel", Type.getMethodType(Types.VOID, Types.ANDROID_PARCEL, Types.INT))
  }

  private fun shouldExcludeFieldFromParcelableClass(access: Int, name: String, description: String, signature: String?): Boolean {
    return name == "CREATOR" && access.isStatic
  }

  private fun shouldExcludeMethodFromParcelableClass(access: Int, name: String, description: String, signature: String?): Boolean {
    return name == "describeContents" || name == "writeToParcel"
  }

  private fun createInterceptedFieldVisitor(delegate: FieldVisitor?, access: Int, name: String, description: String, signature: String?, value: Any?): FieldVisitor? {
    return delegate
  }

  private fun createInterceptedMethodVisitor(delegate: MethodVisitor?, access: Int, name: String, description: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
    return given(name == "<clinit>" && access.isStatic) {
      InitializerBlockInterceptor(delegate, access, Method(name, description)) {
        newInstance(creatorTypeFrom(spec), Methods.getConstructor())
        putStatic(spec.clazz.type, "CREATOR", creatorTypeFrom(spec))
      }
    } ?: delegate
  }

  private inner class InitializerBlockInterceptor(
      private val delegate: MethodVisitor?,
      private val access: Int,
      private val method: Method,
      private val interceptor: GeneratorAdapter.() -> Unit
  ) : GeneratorAdapter(delegate, access, method) {
    override fun visitCode() {
      super.visitCode()
      interceptor()
    }
  }
}
