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

internal interface ValueAdapter {
  fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec)
  fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec)
}

internal object ValueAdapterFactory {
  private val ADAPTERS = HashMap<Type, ValueAdapter>().apply {
    put(Types.BOOLEAN, BooleanValueAdapter)
    put(Types.BYTE, ByteValueAdapter)
    put(Types.CHAR, CharValueAdapter)
    put(Types.DOUBLE, DoubleValueAdapter)
    put(Types.FLOAT, FloatValueAdapter)
    put(Types.INT, IntValueAdapter)
    put(Types.LONG, LongValueAdapter)
    put(Types.SHORT, ShortValueAdapter)
    put(Types.STRING, StringValueAdapter)

    put(Types.BOXED_BOOLEAN, BoxedBooleanValueAdapter)
    put(Types.BOXED_BYTE, BoxedByteValueAdapter)
    put(Types.BOXED_CHAR, BoxedCharValueAdapter)
    put(Types.BOXED_DOUBLE, BoxedDoubleValueAdapter)
    put(Types.BOXED_FLOAT, BoxedFloatValueAdapter)
    put(Types.BOXED_INT, BoxedIntValueAdapter)
    put(Types.BOXED_LONG, BoxedLongValueAdapter)
    put(Types.BOXED_SHORT, BoxedShortValueAdapter)

    put(Types.getArrayType(Types.BOOLEAN), BooleanArrayValueAdapter)
    put(Types.getArrayType(Types.BYTE), ByteArrayValueAdapter)
    put(Types.getArrayType(Types.CHAR), CharArrayValueAdapter)
    put(Types.getArrayType(Types.DOUBLE), DoubleArrayValueAdapter)
    put(Types.getArrayType(Types.FLOAT), FloatArrayValueAdapter)
    put(Types.getArrayType(Types.INT), IntArrayValueAdapter)
    put(Types.getArrayType(Types.LONG), LongArrayValueAdapter)
    put(Types.getArrayType(Types.STRING), StringArrayValueAdapter)
  }

  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    if (property.type == Types.ANDROID_BUNDLE) {
      return BundleValueAdapter
    }

    if (registry.isSubclassOf(property.type, Types.ANDROID_PARCELABLE)) {
      return ParcelableValueAdapter
    }

    if (property.type.sort == Type.ARRAY && property.type.dimensions == 1) {
      if (registry.isSubclassOf(property.type.elementType, Types.ANDROID_PARCELABLE)) {
        return ParcelableArrayValueAdapter
      }
    }

    if (registry.isSubclassOf(property.type, Types.ENUM)) {
      return EnumValueAdapter
    }

    return ADAPTERS.getOrElse(property.type) {
      throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property.name, property.type.className)
    }
  }
}

internal abstract class OptionalValueAdapter() : ValueAdapter {
  final override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.ifZCmp(Opcodes.IFEQ, start)

    adapter.readRequired(variables, property)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.pushNull()

    adapter.mark(end)
  }

  final override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(variables.value())
    adapter.ifNull(start)

    adapter.loadLocal(variables.parcel())
    adapter.push(1)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
    adapter.writeRequired(variables, property)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.loadLocal(variables.parcel())
    adapter.push(0)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))

    adapter.mark(end)
  }

  private fun GeneratorAdapter.readRequired(variables: VariablesContext, property: AutoParcelablePropertySpec) {
    readRequiredValue(this, variables, property)
  }

  private fun GeneratorAdapter.writeRequired(variables: VariablesContext, property: AutoParcelablePropertySpec) {
    writeRequiredValue(this, variables, property)
  }

  abstract fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec)
  abstract fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec)
}

internal open class SimpleValueAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal open class SimpleBoxedValueAdapter(
    private val delegate: ValueAdapter,
    private val unboxed: Type,
    private val boxed: Type,
    private val unboxer: String,
    private val boxer: String
) : OptionalValueAdapter() {
  override fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    delegate.readValue(adapter, variables, property)
    adapter.invokeStatic(boxed, Methods.get(boxer, boxed, unboxed))
  }

  override fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(boxed, Methods.get(unboxer, unboxed))

    val context = VariablesContext(HashMap(variables.names)).apply {
      value(adapter.newLocal(unboxed).apply {
        adapter.storeLocal(this)
      })
    }

    delegate.writeValue(adapter, context, property)
  }
}

internal object ByteValueAdapter : SimpleValueAdapter(Types.BYTE, "readByte", "writeByte")
internal object DoubleValueAdapter : SimpleValueAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatValueAdapter : SimpleValueAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntValueAdapter : SimpleValueAdapter(Types.INT, "readInt", "writeInt")
internal object LongValueAdapter : SimpleValueAdapter(Types.LONG, "readLong", "writeLong")
internal object StringValueAdapter : SimpleValueAdapter(Types.STRING, "readString", "writeString")
internal object BundleValueAdapter : SimpleValueAdapter(Types.ANDROID_BUNDLE, "readBundle", "writeBundle")

internal object BoxedByteValueAdapter : SimpleBoxedValueAdapter(ByteValueAdapter, Types.BYTE, Types.BOXED_BYTE, "byteValue", "valueOf")
internal object BoxedCharValueAdapter : SimpleBoxedValueAdapter(CharValueAdapter, Types.CHAR, Types.BOXED_CHAR, "charValue", "valueOf")
internal object BoxedDoubleValueAdapter : SimpleBoxedValueAdapter(DoubleValueAdapter, Types.DOUBLE, Types.BOXED_DOUBLE, "doubleValue", "valueOf")
internal object BoxedFloatValueAdapter : SimpleBoxedValueAdapter(FloatValueAdapter, Types.FLOAT, Types.BOXED_FLOAT, "floatValue", "valueOf")
internal object BoxedIntValueAdapter : SimpleBoxedValueAdapter(IntValueAdapter, Types.INT, Types.BOXED_INT, "intValue", "valueOf")
internal object BoxedLongValueAdapter : SimpleBoxedValueAdapter(LongValueAdapter, Types.LONG, Types.BOXED_LONG, "longValue", "valueOf")
internal object BoxedShortValueAdapter : SimpleBoxedValueAdapter(ShortValueAdapter, Types.SHORT, Types.BOXED_SHORT, "shortValue", "valueOf")
internal object BoxedBooleanValueAdapter : SimpleBoxedValueAdapter(BooleanValueAdapter, Types.BOOLEAN, Types.BOXED_BOOLEAN, "booleanValue", "valueOf")

internal object BooleanArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.BOOLEAN), "createBooleanArray", "writeBooleanArray")
internal object ByteArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.BYTE), "createByteArray", "writeByteArray")
internal object CharArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.CHAR), "createCharArray", "writeCharArray")
internal object DoubleArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.DOUBLE), "createDoubleArray", "writeDoubleArray")
internal object FloatArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.FLOAT), "createFloatArray", "writeFloatArray")
internal object IntArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.INT), "createIntArray", "writeIntArray")
internal object LongArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.LONG), "createLongArray", "writeLongArray")
internal object StringArrayValueAdapter : SimpleValueAdapter(Types.getArrayType(Types.STRING), "createStringArray", "writeStringArray")

internal object CharValueAdapter : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.CHAR)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.cast(Types.CHAR, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ShortValueAdapter : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.SHORT)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.cast(Types.SHORT, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanValueAdapter : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
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

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
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

internal object EnumValueAdapter : OptionalValueAdapter() {
  override fun readRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.invokeStatic(property.type, Methods.get("values", Types.getArrayType(property.type)))
    adapter.swap(Types.INT, Types.getArrayType(property.type))
    adapter.arrayLoad(property.type)
  }

  override fun writeRequiredValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.invokeVirtual(property.type, Methods.get("ordinal", Types.INT))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ParcelableValueAdapter : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.push(property.type)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelable", Types.ANDROID_PARCELABLE, Types.CLASS_LOADER))
    adapter.checkCast(property.type)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.checkCast(Types.ANDROID_PARCELABLE)
    adapter.loadLocal(variables.flags())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelable", Types.VOID, Types.ANDROID_PARCELABLE, Types.INT))
  }
}

internal object ParcelableArrayValueAdapter : ValueAdapter {
  override fun readValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.push(property.type.elementType)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelableArray", Types.getArrayType(Types.ANDROID_PARCELABLE), Types.CLASS_LOADER))
    adapter.castArray(Types.ANDROID_PARCELABLE, property.type.elementType)
  }

  override fun writeValue(adapter: GeneratorAdapter, variables: VariablesContext, property: AutoParcelablePropertySpec) {
    adapter.loadLocal(variables.parcel())
    adapter.loadLocal(variables.value())
    adapter.checkCast(Types.getArrayType(Types.ANDROID_PARCELABLE))
    adapter.loadLocal(variables.flags())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelableArray", Types.VOID, Types.getArrayType(Types.ANDROID_PARCELABLE), Types.INT))
  }
}
