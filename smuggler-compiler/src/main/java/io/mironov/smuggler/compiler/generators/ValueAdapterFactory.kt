package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.InvalidTypeAdapterException
import io.mironov.smuggler.compiler.annotations.GlobalAdapter
import io.mironov.smuggler.compiler.annotations.LocalAdapter
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.isAbstract
import io.mironov.smuggler.compiler.common.isInterface
import io.mironov.smuggler.compiler.common.isPublic
import io.mironov.smuggler.compiler.common.isSynthetic
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpec
import io.mironov.smuggler.compiler.model.AutoParcelablePropertySpec
import io.mironov.smuggler.compiler.reflect.ClassReference
import io.mironov.smuggler.compiler.reflect.ClassSpec
import io.mironov.smuggler.compiler.signature.ClassSignatureMirror
import io.mironov.smuggler.compiler.signature.GenericType
import io.mironov.smuggler.compiler.signature.MethodSignatureMirror
import org.objectweb.asm.Type
import java.util.Arrays
import kotlin.reflect.jvm.internal.impl.serialization.Flags
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal class ValueAdapterFactory private constructor(
    private val registry: ClassRegistry,
    private val adapters: Map<Type, ValueAdapter>
) {
  companion object {
    private val ADAPTERS = hashMapOf(
        Types.BOOLEAN to BooleanValueAdapter,
        Types.BYTE to ByteValueAdapter,
        Types.CHAR to CharValueAdapter,
        Types.DOUBLE to DoubleValueAdapter,
        Types.FLOAT to FloatValueAdapter,
        Types.INT to IntValueAdapter,
        Types.LONG to LongValueAdapter,
        Types.SHORT to ShortValueAdapter,

        Types.BOXED_BOOLEAN to BoxedBooleanValueAdapter,
        Types.BOXED_BYTE to BoxedByteValueAdapter,
        Types.BOXED_CHAR to BoxedCharValueAdapter,
        Types.BOXED_DOUBLE to BoxedDoubleValueAdapter,
        Types.BOXED_FLOAT to BoxedFloatValueAdapter,
        Types.BOXED_INT to BoxedIntValueAdapter,
        Types.BOXED_LONG to BoxedLongValueAdapter,
        Types.BOXED_SHORT to BoxedShortValueAdapter,

        Types.STRING to StringValueAdapter,
        Types.DATE to DateValueAdapter,

        Types.ANDROID_SPARSE_BOOLEAN_ARRAY to SparseBooleanArrayValueAdapter,
        Types.ANDROID_BUNDLE to BundleValueAdapter
    )

    fun from(registry: ClassRegistry): ValueAdapterFactory {
      val adapters = findTypeAdapterClasses(registry).map { registry.resolve(it) }
      val global = adapters.filter { it.getAnnotation<GlobalAdapter>() != null }

      return ValueAdapterFactory(registry, ADAPTERS + global.associate {
        createAssistedValueAdapter(it, registry)
      })
    }

    fun from(factory: ValueAdapterFactory, spec: AutoParcelableClassSpec): ValueAdapterFactory {
      val locals = spec.clazz.getAnnotation<LocalAdapter>()
      val types = locals?.value().orEmpty()

      return ValueAdapterFactory(factory.registry, factory.adapters + types.associate {
        createAssistedValueAdapter(factory.registry.resolve(it), factory.registry)
      })
    }

    private fun findTypeAdapterClasses(registry: ClassRegistry): Collection<ClassReference> {
      return registry.inputs.filter {
        !it.isInterface && registry.isSubclassOf(it.type, Types.SMUGGLER_ADAPTER)
      }
    }

    private fun createAssistedValueAdapter(spec: ClassSpec, registry: ClassRegistry): Pair<Type, ValueAdapter> {
      if (!registry.isSubclassOf(spec.type, Types.SMUGGLER_ADAPTER)) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must implement TypeAdapter interface")
      }

      if (!spec.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public visibility")
      }

      if (spec.isAbstract) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must be not abstract")
      }

      val constructor = spec.getConstructor()
      val assisted = createAssistedTypeForTypeAdapter(spec, registry)
      val metadata = spec.getAnnotation<Metadata>()

      if (metadata != null) {
        val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
        val clazz = proto.classProto

        if (Flags.CLASS_KIND.get(clazz.flags) == ProtoBuf.Class.Kind.COMPANION_OBJECT) {
          throw InvalidTypeAdapterException(spec.type, "TypeAdapter cannot be a companion object")
        }

        if (Flags.CLASS_KIND.get(clazz.flags) == ProtoBuf.Class.Kind.OBJECT) {
          return assisted to AssistedValueAdapter.fromObject(spec.type, assisted)
        }
      }

      if (constructor == null || !constructor.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public no args constructor")
      }

      return assisted to AssistedValueAdapter.fromClass(spec.type, assisted)
    }

    private fun createAssistedTypeForTypeAdapter(spec: ClassSpec, registry: ClassRegistry): Type {
      val assisted = resolveAssistedType(spec.type, spec.type, registry)

      if (spec.signature != null && !ClassSignatureMirror.read(spec.signature).typeParameters.isEmpty()) {
        throw throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes can't have any type parameters")
      }

      if (assisted !is GenericType.RawType) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must be parameterized with a raw type")
      }

      return assisted.type
    }

    private fun resolveAssistedType(adapter: Type, current: Type, registry: ClassRegistry): GenericType {
      val spec = registry.resolve(current, false)
      val method = spec.methods.singleOrNull {
        !it.isSynthetic && it.name == "fromParcel" && Arrays.equals(it.arguments, arrayOf(Types.ANDROID_PARCEL))
      }

      if (method != null && method.signature != null) {
        return MethodSignatureMirror.read(method.signature).returnType
      }

      if (method != null && method.signature == null) {
        return GenericType.RawType(method.returns)
      }

      if (spec.parent == Types.OBJECT) {
        throw InvalidTypeAdapterException(adapter, "Unable to extract assisted type information")
      }

      return resolveAssistedType(adapter, spec.parent, registry)
    }
  }

  fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    return create(spec, property, property.type)
  }

  fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): ValueAdapter {
    return adapters[generic.asAsmType()] ?: run {
      val type = generic.asAsmType()

      when (type) {
        Types.MAP -> return createMap(Types.MAP, Types.LINKED_MAP, spec, property, generic)
        Types.LINKED_MAP -> return createMap(Types.LINKED_MAP, Types.LINKED_MAP, spec, property, generic)
        Types.HASH_MAP -> return createMap(Types.HASH_MAP, Types.HASH_MAP, spec, property, generic)
        Types.SORTED_MAP -> return createMap(Types.SORTED_MAP, Types.TREE_MAP, spec, property, generic)
        Types.TREE_MAP -> return createMap(Types.TREE_MAP, Types.TREE_MAP, spec, property, generic)

        Types.SET -> return createCollection(Types.SET, Types.LINKED_SET, spec, property, generic)
        Types.LINKED_SET -> return createCollection(Types.LINKED_SET, Types.LINKED_SET, spec, property, generic)
        Types.HASH_SET -> return createCollection(Types.HASH_SET, Types.HASH_SET, spec, property, generic)
        Types.SORTED_SET -> return createCollection(Types.SORTED_SET, Types.TREE_SET, spec, property, generic)
        Types.TREE_SET -> createCollection(Types.TREE_SET, Types.TREE_SET, spec, property, generic)

        Types.LIST -> return createCollection(Types.LIST, Types.ARRAY_LIST, spec, property, generic)
        Types.LINKED_LIST -> return createCollection(Types.LINKED_LIST, Types.LINKED_LIST, spec, property, generic)
        Types.ARRAY_LIST -> return createCollection(Types.ARRAY_LIST, Types.ARRAY_LIST, spec, property, generic)

        Types.COLLECTION -> return createCollection(Types.COLLECTION, Types.ARRAY_LIST, spec, property, generic)
      }

      if (registry.isSubclassOf(type, Types.ENUM)) {
        return EnumValueAdapter
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

  private fun createMap(collection: Type, implementation: Type, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: GenericType): ValueAdapter {
    val adapters = createAdaptersForParameterizedType(spec, property, generic)

    if (adapters.size != 2) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly two type arguments", property.name)
    }

    return MapValueAdapter(collection, implementation, adapters[0], adapters[1])
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
