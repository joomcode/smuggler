package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.ClassRegistry
import io.mironov.smuggler.compiler.SmugglerException
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.model.AutoParcelableClassSpec
import io.mironov.smuggler.compiler.model.AutoParcelablePropertySpec
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.util.HashMap

internal interface PropertyAdapter {
  fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal object PropertyAdapterFactory {
  private val ADAPTERS = HashMap<Type, PropertyAdapter>().apply {
    put(Types.BOOLEAN, BooleanPropertyAdapter)
    put(Types.BYTE, BytePropertyAdapter)
    put(Types.CHAR, CharPropertyAdapter)
    put(Types.DOUBLE, DoublePropertyAdapter)
    put(Types.FLOAT, FloatPropertyAdapter)
    put(Types.INT, IntPropertyAdapter)
    put(Types.LONG, LongPropertyAdapter)
    put(Types.SHORT, ShortPropertyAdapter)
    put(Types.STRING, StringPropertyAdapter)

    put(Types.BOXED_BOOLEAN, BoxedBooleanPropertyAdapter)
    put(Types.BOXED_BYTE, BoxedBytePropertyAdapter)
    put(Types.BOXED_CHAR, BoxedCharPropertyAdapter)
    put(Types.BOXED_DOUBLE, BoxedDoublePropertyAdapter)
    put(Types.BOXED_FLOAT, BoxedFloatPropertyAdapter)
    put(Types.BOXED_INT, BoxedIntPropertyAdapter)
    put(Types.BOXED_LONG, BoxedLongPropertyAdapter)
    put(Types.BOXED_SHORT, BoxedShortPropertyAdapter)

    put(Types.getArrayType(Types.BOOLEAN), BooleanArrayPropertyAdapter)
    put(Types.getArrayType(Types.BYTE), ByteArrayPropertyAdapter)
    put(Types.getArrayType(Types.CHAR), CharArrayPropertyAdapter)
    put(Types.getArrayType(Types.DOUBLE), DoubleArrayPropertyAdapter)
    put(Types.getArrayType(Types.FLOAT), FloatArrayPropertyAdapter)
    put(Types.getArrayType(Types.INT), IntArrayPropertyAdapter)
    put(Types.getArrayType(Types.LONG), LongArrayPropertyAdapter)
    put(Types.getArrayType(Types.STRING), StringArrayPropertyAdapter)
  }

  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): PropertyAdapter {
    if (property.type == Types.ANDROID_BUNDLE) {
      return BundlePropertyAdapter
    }

    if (registry.isSubclassOf(property.type, Types.ANDROID_PARCELABLE)) {
      return ParcelablePropertyAdapter
    }

    if (property.type.sort == Type.ARRAY && property.type.dimensions == 1) {
      if (registry.isSubclassOf(property.type.elementType, Types.ANDROID_PARCELABLE)) {
        return ParcelableArrayPropertyAdapter
      }
    }

    if (registry.isSubclassOf(property.type, Types.ENUM)) {
      return EnumPropertyAdapter
    }

    return ADAPTERS.getOrElse(property.type) {
      throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property.name, property.type.className)
    }
  }
}

internal abstract class OptionalPropertyAdapter() : PropertyAdapter {
  final override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.ifZCmp(Opcodes.IFEQ, start)

    adapter.readRequired(variables, owner, property)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.pushNull()

    adapter.mark(end)
  }

  final override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.value())
    adapter.ifNull(start)

    adapter.loadLocal(variables.parcel())
    adapter.push(1)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
    adapter.writeRequired(variables, owner, property)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.loadLocal(variables.parcel())
    adapter.push(0)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))

    adapter.mark(end)
  }

  private fun GeneratorAdapter.readRequired(variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    readRequiredValue(this, variables, owner, property)
  }

  private fun GeneratorAdapter.writeRequired(variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    writeRequiredValue(this, variables, owner, property)
  }

  abstract fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  abstract fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal open class SimplePropertyAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal open class SimpleBoxedPropertyAdapter(
    private val delegate: PropertyAdapter,
    private val unboxed: Type,
    private val boxed: Type,
    private val unboxer: String,
    private val boxer: String
) : OptionalPropertyAdapter() {
  override fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    delegate.readValue(adapter, variables, owner, property)
    adapter.invokeStatic(boxed, Methods.get(boxer, boxed, unboxed))
  }

  override fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(boxed, Methods.get(unboxer, unboxed))

    val context = VariablesContext(HashMap(variables.names)).apply {
      value(adapter.newLocal(unboxed).apply {
        adapter.storeLocal(this)
      })
    }

    delegate.writeValue(adapter, context, owner, property)
  }
}

internal object BytePropertyAdapter : SimplePropertyAdapter(Types.BYTE, "readByte", "writeByte")
internal object DoublePropertyAdapter : SimplePropertyAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatPropertyAdapter : SimplePropertyAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntPropertyAdapter : SimplePropertyAdapter(Types.INT, "readInt", "writeInt")
internal object LongPropertyAdapter : SimplePropertyAdapter(Types.LONG, "readLong", "writeLong")
internal object StringPropertyAdapter : SimplePropertyAdapter(Types.STRING, "readString", "writeString")
internal object BundlePropertyAdapter : SimplePropertyAdapter(Types.ANDROID_BUNDLE, "readBundle", "writeBundle")

internal object BoxedBytePropertyAdapter : SimpleBoxedPropertyAdapter(BytePropertyAdapter, Types.BYTE, Types.BOXED_BYTE, "byteValue", "valueOf")
internal object BoxedCharPropertyAdapter : SimpleBoxedPropertyAdapter(CharPropertyAdapter, Types.CHAR, Types.BOXED_CHAR, "charValue", "valueOf")
internal object BoxedDoublePropertyAdapter : SimpleBoxedPropertyAdapter(DoublePropertyAdapter, Types.DOUBLE, Types.BOXED_DOUBLE, "doubleValue", "valueOf")
internal object BoxedFloatPropertyAdapter : SimpleBoxedPropertyAdapter(FloatPropertyAdapter, Types.FLOAT, Types.BOXED_FLOAT, "floatValue", "valueOf")
internal object BoxedIntPropertyAdapter : SimpleBoxedPropertyAdapter(IntPropertyAdapter, Types.INT, Types.BOXED_INT, "intValue", "valueOf")
internal object BoxedLongPropertyAdapter : SimpleBoxedPropertyAdapter(LongPropertyAdapter, Types.LONG, Types.BOXED_LONG, "longValue", "valueOf")
internal object BoxedShortPropertyAdapter : SimpleBoxedPropertyAdapter(ShortPropertyAdapter, Types.SHORT, Types.BOXED_SHORT, "shortValue", "valueOf")
internal object BoxedBooleanPropertyAdapter : SimpleBoxedPropertyAdapter(BooleanPropertyAdapter, Types.BOOLEAN, Types.BOXED_BOOLEAN, "booleanValue", "valueOf")

internal object BooleanArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.BOOLEAN), "createBooleanArray", "writeBooleanArray")
internal object ByteArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.BYTE), "createByteArray", "writeByteArray")
internal object CharArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.CHAR), "createCharArray", "writeCharArray")
internal object DoubleArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.DOUBLE), "createDoubleArray", "writeDoubleArray")
internal object FloatArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.FLOAT), "createFloatArray", "writeFloatArray")
internal object IntArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.INT), "createIntArray", "writeIntArray")
internal object LongArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.LONG), "createLongArray", "writeLongArray")
internal object StringArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.STRING), "createStringArray", "writeStringArray")

internal object CharPropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.CHAR)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.cast(Types.CHAR, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ShortPropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.SHORT)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.cast(Types.SHORT, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanPropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.ifZCmp(Opcodes.IFEQ, start)

    adapter.push(true)
    adapter.goTo(end)
    adapter.mark(start)
    adapter.push(false)

    adapter.mark(end)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.ifZCmp(Opcodes.IFEQ, start)
    adapter.push(1)
    adapter.goTo(end)
    adapter.mark(start)
    adapter.push(0)
    adapter.mark(end)

    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object EnumPropertyAdapter : OptionalPropertyAdapter() {
  override fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.invokeStatic(property.type, Methods.get("values", Types.getArrayType(property.type)))
    adapter.swap(Types.INT, Types.getArrayType(property.type))
    adapter.arrayLoad(property.type)
  }

  override fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(property.type, Methods.get("ordinal", Types.INT))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ParcelablePropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.push(property.type)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelable", Types.ANDROID_PARCELABLE, Types.CLASS_LOADER))
    adapter.checkCast(property.type)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.checkCast(Types.ANDROID_PARCELABLE)
    adapter.loadLocal(variables.flags())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelable", Types.VOID, Types.ANDROID_PARCELABLE, Types.INT))
  }
}

internal object ParcelableArrayPropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.push(property.type.elementType)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelableArray", Types.getArrayType(Types.ANDROID_PARCELABLE), Types.CLASS_LOADER))
    adapter.castArray(Types.ANDROID_PARCELABLE, property.type.elementType)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.checkCast(Types.getArrayType(Types.ANDROID_PARCELABLE))
    adapter.loadLocal(variables.flags())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelableArray", Types.VOID, Types.getArrayType(Types.ANDROID_PARCELABLE), Types.INT))
  }
}
