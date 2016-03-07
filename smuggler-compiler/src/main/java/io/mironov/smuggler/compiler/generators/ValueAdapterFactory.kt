package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.InvalidTypeAdapterException
import io.mironov.smuggler.compiler.annotations.AdaptedType
import io.mironov.smuggler.compiler.annotations.GlobalAdapter
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.isAbstract
import io.mironov.smuggler.compiler.common.isInterface
import io.mironov.smuggler.compiler.common.isPublic
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpec
import io.mironov.smuggler.compiler.model.AutoParcelablePropertySpec
import io.mironov.smuggler.compiler.reflect.ClassReference
import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.signature.GenericType
import org.objectweb.asm.Type
import java.util.HashMap

internal class ValueAdapterFactory private constructor(
    private val registry: ClassRegistry,
    private val adapters: Map<Type, ValueAdapter>
) {
  companion object {
    private val ADAPTERS = HashMap<Type, ValueAdapter>().apply {
      put(Types.BOOLEAN, BooleanValueAdapter)
      put(Types.BYTE, ByteValueAdapter)
      put(Types.CHAR, CharValueAdapter)
      put(Types.DOUBLE, DoubleValueAdapter)
      put(Types.FLOAT, FloatValueAdapter)
      put(Types.INT, IntValueAdapter)
      put(Types.LONG, LongValueAdapter)
      put(Types.SHORT, ShortValueAdapter)

      put(Types.BOXED_BOOLEAN, BoxedBooleanValueAdapter)
      put(Types.BOXED_BYTE, BoxedByteValueAdapter)
      put(Types.BOXED_CHAR, BoxedCharValueAdapter)
      put(Types.BOXED_DOUBLE, BoxedDoubleValueAdapter)
      put(Types.BOXED_FLOAT, BoxedFloatValueAdapter)
      put(Types.BOXED_INT, BoxedIntValueAdapter)
      put(Types.BOXED_LONG, BoxedLongValueAdapter)
      put(Types.BOXED_SHORT, BoxedShortValueAdapter)

      put(Types.STRING, StringValueAdapter)
      put(Types.DATE, DateValueAdapter)

      put(Types.ANDROID_SPARSE_BOOLEAN_ARRAY, SparseBooleanArrayValueAdapter)
      put(Types.ANDROID_BUNDLE, BundleValueAdapter)
    }

    fun from(registry: ClassRegistry): ValueAdapterFactory {
      val adapters = findTypeAdapterClasses(registry).map { registry.resolve(it) }
      val global = adapters.filter { it.getAnnotation<GlobalAdapter>() != null }

      return ValueAdapterFactory(registry, ADAPTERS + global.associate {
        createAdaptedValueAdapter(it, registry)
      })
    }

    private fun findTypeAdapterClasses(registry: ClassRegistry): Collection<ClassReference> {
      return registry.inputs.filter {
        !it.isInterface && registry.isSubclassOf(it.type, Types.SMUGGLER_ADAPTER)
      }
    }

    private fun createAdaptedValueAdapter(spec: ClassSpec, registry: ClassRegistry): Pair<Type, ValueAdapter> {
      val constructor = spec.getConstructor()
      val adapted = spec.getAnnotation<AdaptedType>()

      if (constructor == null || !constructor.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public no args constructor")
      }

      if (spec.isAbstract) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must be not abstract")
      }

      if (!spec.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public visibility")
      }

      if (adapted == null) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have @AdaptedType annotation")
      }

      return adapted.value() to AdaptedValueAdapter(spec.type, adapted.value())
    }
  }

  fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    return create(spec, property, property.type)
  }

  fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): ValueAdapter {
    return adapters[generic.asAsmType()] ?: run {
      val type = generic.asAsmType()

      if (registry.isSubclassOf(type, Types.ENUM)) {
        return EnumValueAdapter
      }

      if (type == Types.MAP) {
        return createMap(spec, property, generic)
      }

      if (type == Types.LIST) {
        return createCollection(Types.LIST, Types.ARRAY_LIST, spec, property, generic)
      }

      if (type == Types.SET) {
        return createCollection(Types.SET, Types.LINKED_SET, spec, property, generic)
      }

      if (registry.isSubclassOf(type, Types.ANDROID_SPARSE_ARRAY)) {
        return createSparseArray(spec, property)
      }

      if (registry.isSubclassOf(type, Types.ANDROID_PARCELABLE)) {
        return ParcelableValueAdapter
      }

      if (generic is GenericType.ArrayType) {
        return ArrayPropertyAdapter(create(spec, property, generic.elementType))
      }

      if (type.sort == Type.ARRAY) {
        return ArrayPropertyAdapter(create(spec, property, GenericType.RawType(Types.getElementType(type))))
      }

      if (registry.isSubclassOf(type, Types.SERIALIZABLE)) {
        return SerializableValueAdapter
      }

      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' has unsupported type ''{1}''", property.name, type.className)
    }
  }

  private fun createCollection(collection: Type, implementation: Type, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): ValueAdapter {
    val adapters = createAdaptersForParameterizedType(spec, property, generic)

    if (adapters.size != 1) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly one type argument", property.name)
    }

    return CollectionValueAdapter(collection, implementation, adapters[0])
  }

  private fun createMap(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): ValueAdapter {
    val adapters = createAdaptersForParameterizedType(spec, property, generic)

    if (adapters.size != 2) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly two type arguments", property.name)
    }

    return MapValueAdapter(adapters[0], adapters[1])
  }

  private fun createSparseArray(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    if (property.type !is GenericType.ParameterizedType) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized as ''SparseArray<Foo>''", property.name)
    }

    if (property.type.typeArguments.size != 1) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly one type argument", property.name)
    }

    if (property.type.typeArguments[0] !is GenericType.RawType) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized with a raw type", property.name)
    }

    return SparseArrayValueAdapter(property.type.typeArguments[0].asRawType().type)
  }

  private fun createAdaptersForParameterizedType(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): List<ValueAdapter> {
    if (generic !is GenericType.ParameterizedType) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized", property.name)
    }

    generic.typeArguments.forEach {
      if (it !is GenericType.RawType && it !is GenericType.ParameterizedType && it !is GenericType.ArrayType) {
        throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized with raw or generic type", property.name)
      }
    }

    return generic.typeArguments.map {
      create(spec, property, it)
    }
  }
}
