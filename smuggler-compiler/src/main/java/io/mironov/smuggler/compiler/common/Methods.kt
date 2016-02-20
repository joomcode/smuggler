package io.mironov.smuggler.compiler.common

import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.reflect.MethodSpec
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal object Methods {
  fun get(spec: MethodSpec): Method {
    return Method(spec.name, spec.type.descriptor)
  }

  fun get(name: String, returns: Type, vararg args: Type): Method {
    return Method(name, returns, args)
  }

  fun getConstructor(): Method {
    return Method("<init>", Type.VOID_TYPE, emptyArray())
  }

  fun getConstructor(first: Type, vararg args: Type): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first) + args)
  }

  fun getConstructor(first: ClassSpec, vararg args: ClassSpec): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first.type) + args.map { it.type })
  }

  fun getStaticConstructor(): Method {
    return Method("<clinit>", Type.VOID_TYPE, emptyArray())
  }

  fun asJavaDeclaration(spec: MethodSpec): String {
    return "${spec.name}(${spec.arguments.map { it.className }.joinToString(", ")})"
  }

  fun equalsByJavaDeclaration(left: MethodSpec, right: MethodSpec): Boolean {
    return left.name == right.name && left.type.descriptor == right.type.descriptor
  }
}
