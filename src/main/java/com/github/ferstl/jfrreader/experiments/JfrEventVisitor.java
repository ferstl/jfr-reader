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

  default void startEvent(JfrEventInfo eventInfo) {
  }

  default void endEvent(JfrEventInfo eventInfo) {
  }

  default void visitString(JfrEventInfo eventInfo, String attribute, String value) {
  }

  default void visitNumber(JfrEventInfo eventInfo, String attribute, long value) {
  }

  default void visitNumber(JfrEventInfo eventInfo, String attribute, double value) {
  }

  default void visitBoolean(JfrEventInfo eventInfo, String attribute, boolean value) {
  }

  default void visitMemory(JfrEventInfo eventInfo, String attribute, long value, TypedUnit<?> unit) {
  }

  default void visitTimespan(JfrEventInfo eventInfo, String attribute, long value, LinearUnit unit) {
  }

  default void visitTimestamp(JfrEventInfo eventInfo, String attribute, long value, TimestampUnit unit) {
  }

  default void visitPercentage(JfrEventInfo eventInfo, String attribute, double value) {
  }

  default void visitAddress(JfrEventInfo eventInfo, String attribute, long value) {
  }

  default void visitFrequency(JfrEventInfo eventInfo, String attribute, long value) {
  }

  default void visitLabeledIdentifier(JfrEventInfo eventInfo, String attribute, LabeledIdentifier value) {
  }

  default void visitIMCClassLoader(JfrEventInfo eventInfo, String attribute, IMCClassLoader value) {
  }

  default void visitIMCFrame(JfrEventInfo eventInfo, String attribute, IMCFrame value) {
  }

  default void visitIMCMethod(JfrEventInfo eventInfo, String attribute, IMCMethod value) {
  }

  default void visitIMCModule(JfrEventInfo eventInfo, String attribute, IMCModule value) {
  }

  default void visitIMCOldObject(JfrEventInfo eventInfo, String attribute, IMCOldObject value) {
  }

  default void visitIMCOldObjectArray(JfrEventInfo eventInfo, String attribute, IMCOldObjectArray value) {
  }

  default void visitIMCOldObjectField(JfrEventInfo eventInfo, String attribute, IMCOldObjectField value) {
  }

  default void visitIMCOldObjectGcRoot(JfrEventInfo eventInfo, String attribute, IMCOldObjectGcRoot value) {
  }

  default void visitIMCPackage(JfrEventInfo eventInfo, String attribute, IMCPackage value) {
  }

  default void visitIMCStackTrace(JfrEventInfo eventInfo, String attribute, IMCStackTrace value) {
  }

  default void visitIMCThread(JfrEventInfo eventInfo, String attribute, IMCThread value) {
  }

  default void visitIMCThreadGroup(JfrEventInfo eventInfo, String attribute, IMCThreadGroup value) {
  }

  default void visitIMCType(JfrEventInfo eventInfo, String attribute, IMCType value) {
  }
}
