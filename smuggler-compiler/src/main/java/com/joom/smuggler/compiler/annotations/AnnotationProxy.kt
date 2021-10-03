package com.joom.smuggler.compiler.annotations

import com.joom.grip.mirrors.AnnotationMirror
import com.joom.smuggler.compiler.common.cast
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object AnnotationProxy {
  fun <A> create(clazz: Class<A>, spec: AnnotationMirror): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : InvocationHandler {
      private val cache = HashMap<String, Any?>()

      private val string by lazy(LazyThreadSafetyMode.NONE) {
        cache.map { "${it.key}=${it.value}" }.joinToString(", ")
      }

      init {
        for ((key, value) in spec.values) {
          try {
            cache.put(key, resolve(clazz.getMethod(key).returnType, value))
          } catch (exception: NoSuchMethodException) {
            // just ignore
          }
        }
      }

      override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        if (method.name == "toString" && args.orEmpty().size == 0) {
          return toString()
        }

        if (method.name == "hashCode" && args.orEmpty().size == 0) {
          return hashCode()
        }

        if (method.name == "equals" && args.orEmpty().size == 1) {
          return equals(args.orEmpty()[0])
        }

        return cache[method.name]
      }

      override fun toString(): String {
        return "@${clazz.canonicalName}($string)"
      }
    }))
  }

  private fun resolve(type: Class<*>, value: Any): Any {
    return when {
      isArray(type) -> resolveArray(type.componentType, value)
      isAnnotation(type) -> resolveAnnotation(type, value)
      else -> resolveValue(type, value)
    }
  }

  private fun resolveArray(type: Class<*>, value: Any): Any {
    val array = java.lang.reflect.Array.newInstance(type, 0).cast<Array<Any?>>()

    if (value is Array<*>) {
      return value.cast<Array<Any>>().mapTo(ArrayList()) { resolve(type, it) }.toArray(array)
    }

    if (value is Collection<*>) {
      return value.cast<Collection<Any>>().mapTo(ArrayList()) { resolve(type, it) }.toArray(array)
    }

    throw IllegalArgumentException()
  }

  private fun resolveAnnotation(type: Class<*>, value: Any): Any {
    return create(type, value.cast())
  }

  private fun resolveValue(@Suppress("UNUSED_PARAMETER") type: Class<*>, value: Any): Any {
    return value
  }

  private fun isArray(type: Class<*>): Boolean {
    return type.isArray && !type.componentType.isPrimitive
  }

  private fun isAnnotation(type: Class<*>): Boolean {
    return type.isAnnotation || type.getAnnotation(AnnotationDelegate::class.java) != null
  }
}
