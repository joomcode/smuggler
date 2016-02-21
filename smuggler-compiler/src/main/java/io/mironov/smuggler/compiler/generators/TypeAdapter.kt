package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.SmugglerException
import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import io.mironov.smuggler.compiler.model.DataClassSpec
import io.mironov.smuggler.compiler.model.DataPropertySpec
import org.objectweb.asm.Type

internal interface TypeAdapter {
  fun readValue(adapter: GeneratorAdapter)
  fun writeValue(adapter: GeneratorAdapter)
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
) : TypeAdapter {
  override fun readValue(adapter: GeneratorAdapter) {
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(reader, type))
  }

  override fun writeValue(adapter: GeneratorAdapter) {
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get(writer, Types.VOID, type))
  }
}

internal object ByteTypeAdapter : SimpleTypeAdapter(Types.BYTE, "readByte", "writeByte")
internal object CharTypeAdapter : SimpleTypeAdapter(Types.CHAR, "readChar", "writeChar")
internal object DoubleTypeAdapter : SimpleTypeAdapter(Types.DOUBLE, "readDouble", "writeDouble")
internal object FloatTypeAdapter : SimpleTypeAdapter(Types.FLOAT, "readFloat", "writeFloat")
internal object IntTypeAdapter : SimpleTypeAdapter(Types.INT, "readInt", "writeInt")
internal object LongTypeAdapter : SimpleTypeAdapter(Types.LONG, "readLong", "writeLong")
internal object StringTypeAdapter : SimpleTypeAdapter(Types.STRING, "readString", "writeString")

internal object ShortTypeAdapter : TypeAdapter {
  override fun readValue(adapter: GeneratorAdapter) {
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("readInt", Types.INT))
    adapter.cast(Types.INT, Types.SHORT)
  }

  override fun writeValue(adapter: GeneratorAdapter) {
    adapter.cast(Types.SHORT, Types.INT)
    adapter.invokeVirtual(Types.ANDROID_PARCEL, Methods.get("writeInt", Types.VOID, Types.INT))
  }
}