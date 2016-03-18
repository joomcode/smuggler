package io.mironov.smuggler.compiler.common;

import io.michaelrocks.grip.mirrors.signature.*;

public class GripInvider {
  public static ClassSignatureMirror readClassSignature(final String signature) {
    return ClassSignatureMirrorKt.readClassSignature(signature);
  }

  public static MethodSignatureMirror readMethodSignature(final String signature) {
    return MethodSignatureMirrorKt.readMethodSignature(signature);
  }

  public static GenericType readGenericType(final String signature) {
    return GenericTypeReaderKt.readGenericType(signature);
  }
}
