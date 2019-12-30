package com.github.ferstl.jfrreader.experiments;

import org.openjdk.jmc.common.IMCClassLoader;
import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCMethod;
import org.openjdk.jmc.common.IMCModule;
import org.openjdk.jmc.common.IMCOldObject;
import org.openjdk.jmc.common.IMCOldObjectArray;
import org.openjdk.jmc.common.IMCOldObjectField;
import org.openjdk.jmc.common.IMCOldObjectGcRoot;
import org.openjdk.jmc.common.IMCPackage;
import org.openjdk.jmc.common.IMCStackTrace;
import org.openjdk.jmc.common.IMCThread;
import org.openjdk.jmc.common.IMCThreadGroup;
import org.openjdk.jmc.common.IMCType;
import org.openjdk.jmc.common.unit.LinearUnit;
import org.openjdk.jmc.common.unit.TimestampUnit;
import org.openjdk.jmc.common.unit.TypedUnit;
import org.openjdk.jmc.common.util.LabeledIdentifier;

public interface JfrEventVisitor {

  default void visitString(JfrEventInfo eventInfo, String value) {
  }

  default void visitNumber(JfrEventInfo eventInfo, long value) {
  }

  default void visitNumber(JfrEventInfo eventInfo, double value) {
  }

  default void visitBoolean(JfrEventInfo eventInfo, boolean value) {
  }

  default void visitMemory(JfrEventInfo eventInfo, long value, TypedUnit<?> unit) {
  }

  default void visitTimespan(JfrEventInfo eventInfo, long value, LinearUnit unit) {
  }

  default void visitTimestamp(JfrEventInfo eventInfo, long value, TimestampUnit unit) {
  }

  default void visitPercentage(JfrEventInfo eventInfo, double value) {
  }

  default void visitAddress(JfrEventInfo eventInfo, long value) {
  }

  default void visitFrequency(JfrEventInfo eventInfo, long value) {
  }

  default void visitLabeledIdentifier(JfrEventInfo eventInfo, LabeledIdentifier value) {
  }

  default void visitIMCClassLoader(JfrEventInfo eventInfo, IMCClassLoader value) {
  }

  default void visitIMCFrame(JfrEventInfo eventInfo, IMCFrame value) {
  }

  default void visitIMCMethod(JfrEventInfo eventInfo, IMCMethod value) {
  }

  default void visitIMCModule(JfrEventInfo eventInfo, IMCModule value) {
  }

  default void visitIMCOldObject(JfrEventInfo eventInfo, IMCOldObject value) {
  }

  default void visitIMCOldObjectArray(JfrEventInfo eventInfo, IMCOldObjectArray value) {
  }

  default void visitIMCOldObjectField(JfrEventInfo eventInfo, IMCOldObjectField value) {
  }

  default void visitIMCOldObjectGcRoot(JfrEventInfo eventInfo, IMCOldObjectGcRoot value) {
  }

  default void visitIMCPackage(JfrEventInfo eventInfo, IMCPackage value) {
  }

  default void visitIMCStackTrace(JfrEventInfo eventInfo, IMCStackTrace value) {
  }

  default void visitIMCThread(JfrEventInfo eventInfo, IMCThread value) {
  }

  default void visitIMCThreadGroup(JfrEventInfo eventInfo, IMCThreadGroup value) {
  }

  default void visitIMCType(JfrEventInfo eventInfo, IMCType value) {
  }
}
