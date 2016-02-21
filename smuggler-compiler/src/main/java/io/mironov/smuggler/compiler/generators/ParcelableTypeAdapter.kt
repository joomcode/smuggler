package io.mironov.smuggler.compiler.generators

import io.mironov.smuggler.compiler.common.GeneratorAdapter
import io.mironov.smuggler.compiler.common.Methods
import io.mironov.smuggler.compiler.common.Types
import org.objectweb.asm.Type

internal interface TypeAdapter {
  fun readValue(adapter: GeneratorAdapter)
  fun writeValue(adapter: GeneratorAdapter)
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
