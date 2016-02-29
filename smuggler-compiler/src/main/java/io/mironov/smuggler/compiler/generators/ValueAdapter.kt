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
  fun read(adapter: GeneratorAdapter, context: ValueContext)
  fun write(adapter: GeneratorAdapter, context: ValueContext)
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

    put(Types.BOXED_BOOLEAN, BoxedBooleanValueAdapter)
    put(Types.BOXED_BYTE, BoxedByteValueAdapter)
    put(Types.BOXED_CHAR, BoxedCharValueAdapter)
    put(Types.BOXED_DOUBLE, BoxedDoubleValueAdapter)
    put(Types.BOXED_FLOAT, BoxedFloatValueAdapter)
    put(Types.BOXED_INT, BoxedIntValueAdapter)
    put(Types.BOXED_LONG, BoxedLongValueAdapter)
    put(Types.BOXED_SHORT, BoxedShortValueAdapter)

    put(Types.STRING, StringValueAdapter)
    put(Types.ANDROID_BUNDLE, BundleValueAdapter)
  }

  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): ValueAdapter {
    return from(registry, spec, property.type, property.name)
  }
  
  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, type: Type, property: String): ValueAdapter {
    return ADAPTERS[type] ?: run {
      if (registry.isSubclassOf(type, Types.ENUM)) {
        return EnumValueAdapter
      }

      if (registry.isSubclassOf(type, Types.ANDROID_SPARSE_ARRAY)) {
        return SparseArrayValueAdapter
      }

      if (registry.isSubclassOf(type, Types.ANDROID_SPARSE_BOOLEAN_ARRAY)) {
        return SparseBooleanArrayValueAdapter
      }

      if (registry.isSubclassOf(type, Types.ANDROID_PARCELABLE)) {
        return ParcelableValueAdapter
      }

      if (registry.isSubclassOf(type, Types.SERIALIZABLE)) {
        return SerializableValueAdapter
      }

      if (type.sort == Type.ARRAY) {
        return ArrayPropertyAdapter(from(registry, spec, Types.getElementType(type), property))
      }

      throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property, type.className)
    }
  }
}

internal abstract class OptionalValueAdapter() : ValueAdapter {
  final override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.ifZCmp(Opcodes.IFEQ, start)

    adapter.readNotNullValue(context)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.pushNull()

    adapter.mark(end)
  }

  final override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(context.value())
    adapter.ifNull(start)

    adapter.loadLocal(context.parcel())
    adapter.push(1)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
    adapter.writeNotNullValue(context)
    adapter.goTo(end)

    adapter.mark(start)
    adapter.loadLocal(context.parcel())
    adapter.push(0)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))

    adapter.mark(end)
  }

  private fun GeneratorAdapter.readNotNullValue(context: ValueContext) = readNotNull(this, context)
  private fun GeneratorAdapter.writeNotNullValue(context: ValueContext) = writeNotNull(this, context)

  abstract fun readNotNull(adapter: GeneratorAdapter, context: ValueContext)
  abstract fun writeNotNull(adapter: GeneratorAdapter, context: ValueContext)
}

internal open class SimpleValueAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
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
  override fun readNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    delegate.read(adapter, context)
    adapter.invokeStatic(boxed, Methods.get(boxer, boxed, unboxed))
  }

  override fun writeNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.value())
    adapter.invokeVirtual(boxed, Methods.get(unboxer, unboxed))

    delegate.write(adapter, context.typed(unboxed, null).apply {
      value(adapter.newLocal(unboxed).apply {
        adapter.storeLocal(this)
      })
    })
  }
}

internal object ByteValueAdapter : SimpleValueAdapter(Types.BYTE, "readByte", "writeByte")
internal object DoubleValueAdapter : SimpleValueAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatValueAdapter : SimpleValueAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntValueAdapter : SimpleValueAdapter(Types.INT, "readInt", "writeInt")
internal object LongValueAdapter : SimpleValueAdapter(Types.LONG, "readLong", "writeLong")
internal object StringValueAdapter : SimpleValueAdapter(Types.STRING, "readString", "writeString")
internal object BundleValueAdapter : SimpleValueAdapter(Types.ANDROID_BUNDLE, "readBundle", "writeBundle")
internal object SparseBooleanArrayValueAdapter : SimpleValueAdapter(Types.ANDROID_SPARSE_BOOLEAN_ARRAY, "readSparseBooleanArray", "writeSparseBooleanArray")

internal object BoxedByteValueAdapter : SimpleBoxedValueAdapter(ByteValueAdapter, Types.BYTE, Types.BOXED_BYTE, "byteValue", "valueOf")
internal object BoxedCharValueAdapter : SimpleBoxedValueAdapter(CharValueAdapter, Types.CHAR, Types.BOXED_CHAR, "charValue", "valueOf")
internal object BoxedDoubleValueAdapter : SimpleBoxedValueAdapter(DoubleValueAdapter, Types.DOUBLE, Types.BOXED_DOUBLE, "doubleValue", "valueOf")
internal object BoxedFloatValueAdapter : SimpleBoxedValueAdapter(FloatValueAdapter, Types.FLOAT, Types.BOXED_FLOAT, "floatValue", "valueOf")
internal object BoxedIntValueAdapter : SimpleBoxedValueAdapter(IntValueAdapter, Types.INT, Types.BOXED_INT, "intValue", "valueOf")
internal object BoxedLongValueAdapter : SimpleBoxedValueAdapter(LongValueAdapter, Types.LONG, Types.BOXED_LONG, "longValue", "valueOf")
internal object BoxedShortValueAdapter : SimpleBoxedValueAdapter(ShortValueAdapter, Types.SHORT, Types.BOXED_SHORT, "shortValue", "valueOf")
internal object BoxedBooleanValueAdapter : SimpleBoxedValueAdapter(BooleanValueAdapter, Types.BOOLEAN, Types.BOXED_BOOLEAN, "booleanValue", "valueOf")

internal object CharValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.CHAR)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.cast(Types.CHAR, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ShortValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.SHORT)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.cast(Types.SHORT, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.ifZCmp(Opcodes.IFEQ, start)

    adapter.push(true)
    adapter.goTo(end)
    adapter.mark(start)
    adapter.push(false)

    adapter.mark(end)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    val start = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
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
  override fun readNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.invokeStatic(context.type, Methods.get("values", Types.getArrayType(context.type)))
    adapter.swap(Types.INT, Types.getArrayType(context.type))
    adapter.arrayLoad(context.type)
  }

  override fun writeNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.invokeVirtual(context.type, Methods.get("ordinal", Types.INT))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object SerializableValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readSerializable", Types.SERIALIZABLE))
    adapter.checkCast(context.type)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.checkCast(Types.SERIALIZABLE)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeSerializable", Types.VOID, Types.SERIALIZABLE))
  }
}

internal object ParcelableValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.push(context.type)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelable", Types.ANDROID_PARCELABLE, Types.CLASS_LOADER))
    adapter.checkCast(context.type)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.checkCast(Types.ANDROID_PARCELABLE)
    adapter.loadLocal(context.flags())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelable", Types.VOID, Types.ANDROID_PARCELABLE, Types.INT))
  }
}

internal class ArrayPropertyAdapter(
    private val delegate: ValueAdapter
) : OptionalValueAdapter() {
  final override fun readNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    val index = adapter.newLocal(Types.INT)
    val length = adapter.newLocal(Types.INT)
    val elements = adapter.newLocal(context.type)

    val begin = adapter.newLabel()
    val body = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.loadLocal(context.parcel())
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.storeLocal(length)

    adapter.loadLocal(length)
    adapter.newArray(Types.getElementType(context.type))
    adapter.storeLocal(elements)

    adapter.push(0)
    adapter.storeLocal(index)

    adapter.mark(begin)
    adapter.loadLocal(index)
    adapter.loadLocal(length)

    adapter.ifICmp(Opcodes.IFLT, body)
    adapter.goTo(end)

    adapter.mark(body)
    adapter.loadLocal(elements)
    adapter.loadLocal(index)
    adapter.readElement(context.typed(Types.getElementType(context.type), null))
    adapter.arrayStore(Types.getElementType(context.type))

    adapter.iinc(index, 1)
    adapter.goTo(begin)
    adapter.mark(end)

    adapter.loadLocal(elements)
  }

  final override fun writeNotNull(adapter: GeneratorAdapter, context: ValueContext) {
    val index = adapter.newLocal(Types.INT)
    val length = adapter.newLocal(Types.INT)
    val element = adapter.newLocal(Types.getElementType(context.type))

    val begin = adapter.newLabel()
    val body = adapter.newLabel()
    val end = adapter.newLabel()

    adapter.push(0)
    adapter.storeLocal(index)

    adapter.loadLocal(context.value())
    adapter.arrayLength()
    adapter.storeLocal(length)

    adapter.loadLocal(context.parcel())
    adapter.loadLocal(length)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))

    adapter.mark(begin)
    adapter.loadLocal(index)
    adapter.loadLocal(length)

    adapter.ifICmp(Opcodes.IFLT, body)
    adapter.goTo(end)

    adapter.mark(body)
    adapter.loadLocal(context.value())
    adapter.loadLocal(index)
    adapter.arrayLoad(Types.getElementType(context.type))
    adapter.storeLocal(element)
    adapter.writeElement(context.typed(Types.getElementType(context.type), null).apply {
      value(element)
    })

    adapter.iinc(index, 1)
    adapter.goTo(begin)
    adapter.mark(end)
  }

  private fun GeneratorAdapter.readElement(context: ValueContext) = delegate.read(this, context)
  private fun GeneratorAdapter.writeElement(context: ValueContext) = delegate.write(this, context)
}

internal object SparseArrayValueAdapter : ValueAdapter {
  override fun read(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.pushNull()
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readSparseArray", Types.ANDROID_SPARSE_ARRAY, Types.CLASS_LOADER))
    adapter.checkCast(context.type)
  }

  override fun write(adapter: GeneratorAdapter, context: ValueContext) {
    adapter.loadLocal(context.parcel())
    adapter.loadLocal(context.value())
    adapter.checkCast(Types.ANDROID_SPARSE_ARRAY)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeSparseArray", Types.VOID, Types.ANDROID_SPARSE_ARRAY))
  }
}
