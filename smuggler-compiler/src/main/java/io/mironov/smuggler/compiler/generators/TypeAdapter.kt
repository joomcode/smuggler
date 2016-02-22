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

internal interface TypeAdapter {
  fun readValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
  fun writeValue(adapter: GeneratorAdapter, owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec)
}

internal abstract class AbstractTypeAdapter : TypeAdapter {
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

internal object TypeAdapterFactory {
  fun from(registry: ClassRegistry, spec: AutoParcelableClassSpec, property: AutoParcelablePropertySpec): TypeAdapter {
    if (property.type == Types.ANDROID_BUNDLE) {
      return BundleTypeAdapter
    }

    if (registry.isSubclassOf(property.type, Types.ANDROID_PARCELABLE)) {
      return ParcelableTypeAdapter
    }

    return when (property.type) {
      Types.BOOLEAN -> BooleanTypeAdapter
      Types.BYTE -> ByteTypeAdapter
      Types.CHAR -> CharTypeAdapter
      Types.DOUBLE -> DoubleTypeAdapter
      Types.FLOAT -> FloatTypeAdapter
      Types.INT -> IntTypeAdapter
      Types.LONG -> LongTypeAdapter
      Types.SHORT -> ShortTypeAdapter
      Types.STRING -> StringTypeAdapter

      Types.getArrayType(Types.BOOLEAN) -> BooleanArrayTypeAdapter
      Types.getArrayType(Types.BYTE) -> ByteArrayTypeAdapter
      Types.getArrayType(Types.CHAR) -> CharArrayTypeAdapter
      Types.getArrayType(Types.DOUBLE) -> DoubleArrayTypeAdapter
      Types.getArrayType(Types.FLOAT) -> FloatArrayTypeAdapter
      Types.getArrayType(Types.INT) -> IntArrayTypeAdapter
      Types.getArrayType(Types.LONG) -> LongArrayTypeAdapter
      Types.getArrayType(Types.STRING) -> StringArrayTypeAdapter

      else -> throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property.name, property.type.className)
    }
  }
}

internal open class SimpleTypeAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal object ByteTypeAdapter : SimpleTypeAdapter(Types.BYTE, "readByte", "writeByte")
internal object DoubleTypeAdapter : SimpleTypeAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatTypeAdapter : SimpleTypeAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntTypeAdapter : SimpleTypeAdapter(Types.INT, "readInt", "writeInt")
internal object LongTypeAdapter : SimpleTypeAdapter(Types.LONG, "readLong", "writeLong")
internal object StringTypeAdapter : SimpleTypeAdapter(Types.STRING, "readString", "writeString")
internal object BundleTypeAdapter : SimpleTypeAdapter(Types.ANDROID_BUNDLE, "readBundle", "writeBundle")

internal object BooleanArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.BOOLEAN), "createBooleanArray", "writeBooleanArray")
internal object ByteArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.BYTE), "createByteArray", "writeByteArray")
internal object CharArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.CHAR), "createCharArray", "writeCharArray")
internal object DoubleArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.DOUBLE), "createDoubleArray", "writeDoubleArray")
internal object FloatArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.FLOAT), "createFloatArray", "writeFloatArray")
internal object IntArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.INT), "createIntArray", "writeIntArray")
internal object LongArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.LONG), "createLongArray", "writeLongArray")
internal object StringArrayTypeAdapter : SimpleTypeAdapter(Types.getArrayType(Types.STRING), "createStringArray", "writeStringArray")

internal object CharTypeAdapter : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    cast(Types.INT, Types.CHAR)
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    cast(Types.CHAR, Types.INT)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object ShortTypeAdapter : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    cast(Types.INT, Types.SHORT)
  }

  override fun GeneratorAdapter.writeProperty(owner: AutoParcelableClassSpec, property: AutoParcelablePropertySpec) {
    cast(Types.SHORT, Types.INT)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanTypeAdapter : AbstractTypeAdapter() {
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

internal object ParcelableTypeAdapter : TypeAdapter {
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
