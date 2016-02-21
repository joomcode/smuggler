package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.SmugglerException
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.model.DataClassSpec
import io.mironov.smuggler.compiler.model.DataPropertySpec
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal interface TypeAdapter {
  fun readValue(adapter: GeneratorAdapter, owner: DataClassSpec, property: DataPropertySpec)
  fun writeValue(adapter: GeneratorAdapter, owner: DataClassSpec, property: DataPropertySpec)
}

internal abstract class AbstractTypeAdapter : TypeAdapter {
  final override fun readValue(adapter: GeneratorAdapter, owner: DataClassSpec, property: DataPropertySpec) {
    adapter.loadArg(0)
    adapter.readProperty(owner, property)
  }

  final override fun writeValue(adapter: GeneratorAdapter, owner: DataClassSpec, property: DataPropertySpec) {
    adapter.loadArg(0)
    adapter.loadThis()

    adapter.invokeVirtual(owner.clazz, property.getter)
    adapter.writeProperty(owner, property)
  }

  abstract fun GeneratorAdapter.readProperty(owner: DataClassSpec, property: DataPropertySpec)
  abstract fun GeneratorAdapter.writeProperty(owner: DataClassSpec, property: DataPropertySpec)
}

internal object TypeAdapterFactory {
  fun from(spec: DataClassSpec, property: DataPropertySpec): TypeAdapter {
    return when (property.type) {
      Types.BYTE -> ByteTypeAdapter
      Types.CHAR -> CharTypeAdapter
      Types.DOUBLE -> DoubleTypeAdapter
      Types.FLOAT -> FloatTypeAdapter
      Types.INT -> IntTypeAdapter
      Types.LONG -> LongTypeAdapter
      Types.SHORT -> ShortTypeAdapter
      Types.BOOLEAN -> BooleanTypeAdapter
      Types.STRING -> StringTypeAdapter
      else -> throw SmugglerException("Invalid AutoParcelable class ''{0}'', property ''{1}'' has unsupported type ''{2}''",
          spec.clazz.type.className, property.name, property.type)
    }
  }
}

internal open class SimpleTypeAdapter(
    private val type: Type,
    private val reader: String,
    private val writer: String
) : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: DataClassSpec, property: DataPropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun GeneratorAdapter.writeProperty(owner: DataClassSpec, property: DataPropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal object ByteTypeAdapter : SimpleTypeAdapter(Types.BYTE, "readByte", "writeByte")
internal object CharTypeAdapter : SimpleTypeAdapter(Types.CHAR, "readChar", "writeChar")
internal object DoubleTypeAdapter : SimpleTypeAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatTypeAdapter : SimpleTypeAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntTypeAdapter : SimpleTypeAdapter(Types.INT, "readInt", "writeInt")
internal object LongTypeAdapter : SimpleTypeAdapter(Types.LONG, "readLong", "writeLong")
internal object StringTypeAdapter : SimpleTypeAdapter(Types.STRING, "readString", "writeString")

internal object ShortTypeAdapter : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: DataClassSpec, property: DataPropertySpec) {
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    cast(Types.INT, Types.SHORT)
  }

  override fun GeneratorAdapter.writeProperty(owner: DataClassSpec, property: DataPropertySpec) {
    cast(Types.SHORT, Types.INT)
    invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}

internal object BooleanTypeAdapter : AbstractTypeAdapter() {
  override fun GeneratorAdapter.readProperty(owner: DataClassSpec, property: DataPropertySpec) {
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

  override fun GeneratorAdapter.writeProperty(owner: DataClassSpec, property: DataPropertySpec) {
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
