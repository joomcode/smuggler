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

internal interface PropertyAdapter {
  fun readValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  fun writeValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal abstract class AbstractPropertyAdapter : PropertyAdapter {
  final override fun readValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadArg(0)
    adapter.readProperty(owner, property)
  }

  final override fun writeValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadArg(0)
    adapter.loadThis()

    adapter.invokeVirtual(owner.clazz, property.getter)
    adapter.writeProperty(owner, property)
  }

  abstract fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  abstract fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal object PropertyAdapterFactory {
  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): PropertyAdapter {
    if (property.type == Types.ANDROID_BUNDLE) {
      return BundlePropertyAdapter
    }

    if (registry.isSubclassOf(property.type, Types.ANDROID_PARCELABLE)) {
      return ParcelablePropertyAdapter
    }

    if (registry.isSubclassOf(property.type, Types.ENUM)) {
      return EnumPropertyAdapter
    }

    return when (property.type) {
      Types.BOOLEAN -> BooleanPropertyAdapter
      Types.BYTE -> BytePropertyAdapter
      Types.CHAR -> CharPropertyAdapter
      Types.DOUBLE -> DoublePropertyAdapter
      Types.FLOAT -> FloatPropertyAdapter
      Types.INT -> IntPropertyAdapter
      Types.LONG -> LongPropertyAdapter
      Types.SHORT -> ShortPropertyAdapter
      Types.STRING -> StringPropertyAdapter

      Types.BOXED_BOOLEAN -> BoxedBooleanPropertyAdapter

      Types.getArrayType(Types.BOOLEAN) -> BooleanArrayPropertyAdapter
      Types.getArrayType(Types.BYTE) -> ByteArrayPropertyAdapter
      Types.getArrayType(Types.CHAR) -> CharArrayPropertyAdapter
      Types.getArrayType(Types.DOUBLE) -> DoubleArrayPropertyAdapter
      Types.getArrayType(Types.FLOAT) -> FloatArrayPropertyAdapter
      Types.getArrayType(Types.INT) -> IntArrayPropertyAdapter
      Types.getArrayType(Types.LONG) -> LongArrayPropertyAdapter
      Types.getArrayType(Types.STRING) -> StringArrayPropertyAdapter

      else -> throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property.name, property.type.className)
    }
  }
}

internal open class SimplePropertyAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : AbstractPropertyAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal abstract class OptionalPropertyAdapter() : AbstractPropertyAdapter() {
  final override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = newLabel()
    val end = newLabel()

    dup()
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    ifZCmp(Opcodes.IFEQ, start)

    readRequiredProperty(owner, property)
    goTo(end)

    mark(start)
    pop()
    pushNull()

    mark(end)
  }

  final override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = newLabel()
    val end = newLabel()

    dup()
    ifNull(start)

    swap(Types.ANDROID_PARCEL, property.type)
    dup()
    push(1)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
    swap(property.type, Types.ANDROID_PARCEL)
    writeRequiredProperty(owner, property)
    goTo(end)

    mark(start)
    pop()
    push(0)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))

    mark(end)
  }

  abstract fun GeneratorAdapter.readRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  abstract fun GeneratorAdapter.writeRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal object BytePropertyAdapter : SimplePropertyAdapter(Types.BYTE, "readByte", "writeByte")
internal object DoublePropertyAdapter : SimplePropertyAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatPropertyAdapter : SimplePropertyAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntPropertyAdapter : SimplePropertyAdapter(Types.INT, "readInt", "writeInt")
internal object LongPropertyAdapter : SimplePropertyAdapter(Types.LONG, "readLong", "writeLong")
internal object StringPropertyAdapter : SimplePropertyAdapter(Types.STRING, "readString", "writeString")
internal object BundlePropertyAdapter : SimplePropertyAdapter(Types.ANDROID_BUNDLE, "readBundle", "writeBundle")

internal object BooleanArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.BOOLEAN), "createBooleanArray", "writeBooleanArray")
internal object ByteArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.BYTE), "createByteArray", "writeByteArray")
internal object CharArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.CHAR), "createCharArray", "writeCharArray")
internal object DoubleArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.DOUBLE), "createDoubleArray", "writeDoubleArray")
internal object FloatArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.FLOAT), "createFloatArray", "writeFloatArray")
internal object IntArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.INT), "createIntArray", "writeIntArray")
internal object LongArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.LONG), "createLongArray", "writeLongArray")
internal object StringArrayPropertyAdapter : SimplePropertyAdapter(Types.getArrayType(Types.STRING), "createStringArray", "writeStringArray")

internal object CharPropertyAdapter : AbstractPropertyAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    cast(Types.INT, Types.CHAR)
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    cast(Types.CHAR, Types.INT)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ShortPropertyAdapter : AbstractPropertyAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    cast(Types.INT, Types.SHORT)
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    cast(Types.SHORT, Types.INT)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanPropertyAdapter : AbstractPropertyAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = newLabel()
    val end = newLabel()

    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    ifZCmp(Opcodes.IFEQ, start)

    push(true)
    goTo(end)
    mark(start)
    push(false)

    mark(end)
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    val start = newLabel()
    val end = newLabel()

    ifZCmp(Opcodes.IFEQ, start)
    push(1)
    goTo(end)
    mark(start)
    push(0)
    mark(end)

    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ParcelablePropertyAdapter : PropertyAdapter {
  override fun readValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadArg(0)
    adapter.push(property.type)
    adapter.invokeVirtual(Types.CLASS, Methods.get("getClassLoader", Types.CLASS_LOADER))

    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readParcelable", Types.ANDROID_PARCELABLE, Types.CLASS_LOADER))
    adapter.checkCast(property.type)
  }

  override fun writeValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    adapter.loadArg(0)
    adapter.loadThis()

    adapter.invokeVirtual(owner.clazz, property.getter)
    adapter.checkCast(Types.ANDROID_PARCELABLE)
    adapter.loadArg(1)

    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeParcelable", Types.VOID, Types.ANDROID_PARCELABLE, Types.INT))
  }
}

internal object EnumPropertyAdapter : OptionalPropertyAdapter() {
  override fun GeneratorAdapter.readRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    invokeStatic(property.type, Methods.get("values", Types.getArrayType(property.type)))
    swap(Types.INT, Types.getArrayType(property.type))
    arrayLoad(property.type)
  }

  override fun GeneratorAdapter.writeRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(property.type, Methods.get("ordinal", Types.INT))
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BoxedBooleanPropertyAdapter : OptionalPropertyAdapter() {
  override fun GeneratorAdapter.readRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    BooleanPropertyAdapter.apply { readProperty(owner, property) }
    invokeStatic(Types.BOXED_BOOLEAN, Methods.get("valueOf", Types.BOXED_BOOLEAN, Types.BOOLEAN))
  }

  override fun GeneratorAdapter.writeRequiredProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.BOXED_BOOLEAN, Methods.get("booleanValue", Types.BOOLEAN))
    BooleanPropertyAdapter.apply { writeProperty(owner, property) }
  }
}
