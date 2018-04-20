package io.mironov.smuggler.compiler.generators

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.isAbstract
import io.michaelrocks.grip.mirrors.isPublic
import io.michaelrocks.grip.mirrors.isSynthetic
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.toAsmType
import io.michaelrocks.grip.mirrors.toType
import io.mironov.smuggler.compiler.InvalidAutoParcelableException
import io.mironov.smuggler.compiler.InvalidTypeAdapterException
import io.mironov.smuggler.compiler.annotations.LocalAdapter
import io.mironov.smuggler.compiler.annotations.Metadata
import io.mironov.smuggler.compiler.annotations.data
import io.mironov.smuggler.compiler.annotations.strings
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.common.cast
import io.mironov.smuggler.compiler.common.getAnnotation
import io.mironov.smuggler.compiler.common.getDeclaredConstructor
import io.mironov.smuggler.compiler.common.isGlobalTypeAdapter
import io.mironov.smuggler.compiler.common.isSubclassOf
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpec
import io.mironov.smuggler.compiler.model.AutoParcelablePropertySpec
import io.mironov.smuggler.compiler.model.KotlinType
import org.objectweb.asm.Type
import java.io.File
import java.util.Arrays
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf
import kotlin.reflect.jvm.internal.impl.metadata.deserialization.Flags
import kotlin.reflect.jvm.internal.impl.metadata.jvm.deserialization.JvmProtoBufUtil
import io.michaelrocks.grip.mirrors.Type as GripType

internal class ValueAdapterFactory private constructor(
    private val grip: Grip,
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
        Types.CHAR_SEQUENCE to CharSequenceValueAdapter,

        Types.ANDROID_SPARSE_BOOLEAN_ARRAY to SparseBooleanArrayValueAdapter,
        Types.ANDROID_BUNDLE to BundleValueAdapter
    )

    fun from(grip: Grip, files: Collection<File>): ValueAdapterFactory {
      return ValueAdapterFactory(grip, ADAPTERS + grip.select(classes)
          .from(files)
          .where(isGlobalTypeAdapter())
          .execute().values
          .associate { createAssistedValueAdapter(it, grip) }
      )
    }

    fun from(factory: ValueAdapterFactory, spec: AutoParcelableClassSpec): ValueAdapterFactory {
      val locals = spec.clazz.getAnnotation<LocalAdapter>()
      val types = locals?.value().orEmpty()

      return ValueAdapterFactory(factory.grip, factory.adapters + types.associate {
        createAssistedValueAdapter(factory.grip.classRegistry.getClassMirror(GripType.Object(it.toAsmType())), factory.grip)
      })
    }

    private fun createAssistedValueAdapter(spec: ClassMirror, grip: Grip): Pair<Type, ValueAdapter> {
      if (!grip.isSubclassOf(spec.type.toAsmType(), Types.SMUGGLER_ADAPTER)) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must implement TypeAdapter interface")
      }

      if (!spec.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public visibility")
      }

      if (spec.isAbstract) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must be not abstract")
      }

      if (!spec.signature.typeParameters.isEmpty()) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes can''t have any type parameters")
      }

      val constructor = spec.getDeclaredConstructor()
      val assisted = createAssistedTypeForTypeAdapter(spec, grip)
      val metadata = spec.getAnnotation<Metadata>()

      if (metadata != null) {
        val proto = JvmProtoBufUtil.readClassDataFrom(metadata.data, metadata.strings)
        val clazz = proto.component2()

        if (Flags.CLASS_KIND.get(clazz.flags) == ProtoBuf.Class.Kind.COMPANION_OBJECT) {
          throw InvalidTypeAdapterException(spec.type, "TypeAdapter cannot be a companion object")
        }

        if (Flags.CLASS_KIND.get(clazz.flags) == ProtoBuf.Class.Kind.OBJECT) {
          return assisted to AssistedValueAdapter.fromObject(spec.type.toAsmType(), assisted)
        }
      }

      if (constructor == null || !constructor.isPublic) {
        throw InvalidTypeAdapterException(spec.type, "TypeAdapter classes must have public no args constructor")
      }

      return assisted to AssistedValueAdapter.fromClass(spec.type.toAsmType(), assisted)
    }

    private fun createAssistedTypeForTypeAdapter(spec: ClassMirror, grip: Grip): Type {
      return resolveAssistedType(spec.type.toAsmType(), spec.type.toAsmType(), grip)
    }

    private fun resolveAssistedType(adapter: Type, current: Type, grip: Grip): Type {
      val spec = grip.classRegistry.getClassMirror(GripType.Object(current))
      val parent = spec.superType

      val method = spec.methods.singleOrNull {
        !it.isSynthetic && it.name == "fromParcel" && Arrays.equals(it.type.toAsmType().argumentTypes, arrayOf(Types.ANDROID_PARCEL))
      }

      if (method != null && method.signature.returnType !is GenericType.Raw) {
        throw InvalidTypeAdapterException(adapter.toType(), "Unable to extract assisted type information")
      }

      if (method != null && method.signature.returnType is GenericType.Raw) {
        return method.signature.returnType.cast<GenericType.Raw>().type.toAsmType()
      }

      if (parent == null) {
        throw InvalidTypeAdapterException(adapter.toType(), "Unable to extract assisted type information")
      }

      return resolveAssistedType(adapter, parent.toAsmType(), grip)
    }
  }

  fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    return create(spec, property, property.type)
  }

  fun create(spec: AutoParcelableClassSpec): ValueAdapter {
    return when (spec) {
      is AutoParcelableClassSpec.Data -> AutoParcelableClassValueAdapter(spec, this)
      is AutoParcelableClassSpec.Object -> AutoParcelableObjectValueAdapter(spec)
    }
  }

  private fun create(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: KotlinType): ValueAdapter {
    return adapters[generic.asAsmType()] ?: run {
      val type = generic.asAsmType()

      if (generic is KotlinType.Array) {
        return@run ArrayPropertyAdapter(create(spec, property, generic.elementType))
      }

      when (type) {
        Types.MAP -> return@run createMap(Types.MAP, Types.LINKED_MAP, spec, property, generic)
        Types.LINKED_MAP -> return@run createMap(Types.LINKED_MAP, Types.LINKED_MAP, spec, property, generic)
        Types.HASH_MAP -> return@run createMap(Types.HASH_MAP, Types.HASH_MAP, spec, property, generic)
        Types.SORTED_MAP -> return@run createMap(Types.SORTED_MAP, Types.TREE_MAP, spec, property, generic)
        Types.TREE_MAP -> return@run createMap(Types.TREE_MAP, Types.TREE_MAP, spec, property, generic)

        Types.SET -> return@run createCollection(Types.SET, Types.LINKED_SET, spec, property, generic)
        Types.LINKED_SET -> return@run createCollection(Types.LINKED_SET, Types.LINKED_SET, spec, property, generic)
        Types.HASH_SET -> return@run createCollection(Types.HASH_SET, Types.HASH_SET, spec, property, generic)
        Types.SORTED_SET -> return@run createCollection(Types.SORTED_SET, Types.TREE_SET, spec, property, generic)
        Types.TREE_SET -> createCollection(Types.TREE_SET, Types.TREE_SET, spec, property, generic)

        Types.LIST -> return@run createCollection(Types.LIST, Types.ARRAY_LIST, spec, property, generic)
        Types.LINKED_LIST -> return@run createCollection(Types.LINKED_LIST, Types.LINKED_LIST, spec, property, generic)
        Types.ARRAY_LIST -> return@run createCollection(Types.ARRAY_LIST, Types.ARRAY_LIST, spec, property, generic)

        Types.COLLECTION -> return@run createCollection(Types.COLLECTION, Types.ARRAY_LIST, spec, property, generic)
      }

      if (grip.isSubclassOf(type, Types.ENUM)) {
        return@run EnumValueAdapter
      }

      if (grip.isSubclassOf(type, Types.ANDROID_SPARSE_ARRAY)) {
        return@run createSparseArray(spec, property)
      }

      if (grip.isSubclassOf(type, Types.ANDROID_PARCELABLE)) {
        return@run ParcelableValueAdapter
      }

      if (grip.isSubclassOf(type, Types.SERIALIZABLE)) {
        return@run SerializableValueAdapter
      }

      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' has unsupported type ''{1}''", property.name, type.className)
    }
  }

  private fun createCollection(collection: Type, implementation: Type, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: KotlinType): ValueAdapter {
    val adapters = createAdaptersForParameterizedType(spec, property, generic)

    if (adapters.size != 1) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly one type argument", property.name)
    }

    return CollectionValueAdapter(collection, implementation, adapters[0])
  }

  private fun createMap(collection: Type, implementation: Type, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: KotlinType): ValueAdapter {
    val adapters = createAdaptersForParameterizedType(spec, property, generic)

    if (adapters.size != 2) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly two type arguments", property.name)
    }

    return MapValueAdapter(collection, implementation, adapters[0], adapters[1])
  }

  private fun createSparseArray(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    if (property.type !is KotlinType.Parameterized) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized as ''SparseArray<Foo>''", property.name)
    }

    if (property.type.typeArguments.size != 1) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must have exactly one type argument", property.name)
    }

    if (property.type.typeArguments[0] !is KotlinType.Raw) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized with a raw type", property.name)
    }

    return SparseArrayValueAdapter(property.type.typeArguments[0].asRawType().type)
  }

  private fun createAdaptersForParameterizedType(spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec, generic: KotlinType): List<ValueAdapter> {
    if (generic !is KotlinType.Parameterized) {
      throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized", property.name)
    }

    generic.typeArguments.forEach {
      if (it !is KotlinType.Raw && it !is KotlinType.Parameterized && it !is KotlinType.Array) {
        throw InvalidAutoParcelableException(spec.clazz.type, "Property ''{0}'' must be parameterized with raw or generic type", property.name)
      }
    }

    return generic.typeArguments.map {
      create(spec, property, it)
    }
  }
}
