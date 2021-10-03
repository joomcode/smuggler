package com.joom.smuggler.compiler.common

import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.MethodMirror
import com.joom.grip.mirrors.toAsmType
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal object Methods {
  fun get(spec: MethodMirror): Method {
    return Method(spec.name, spec.type.descriptor)
  }

  fun get(name: String, returns: Type, vararg args: Type): Method {
    return Method(name, returns, args)
  }

  fun getConstructor(): Method {
    return Method("<init>", Type.VOID_TYPE, emptyArray())
  }

  fun getConstructor(args: List<Type>): Method {
    return Method("<init>", Type.VOID_TYPE, args.toTypedArray())
  }

  fun getConstructor(first: Type, vararg args: Type): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first) + args)
  }

  fun getConstructor(first: ClassMirror, vararg args: ClassMirror): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first.type.toAsmType()) + args.map { it.type.toAsmType() })
  }

  fun getStaticConstructor(): Method {
    return Method("<clinit>", Type.VOID_TYPE, emptyArray())
  }
}
