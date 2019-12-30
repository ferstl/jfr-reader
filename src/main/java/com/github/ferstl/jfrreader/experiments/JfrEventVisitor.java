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
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.LinearUnit;
import org.openjdk.jmc.common.unit.TimestampUnit;
import org.openjdk.jmc.common.unit.TypedUnit;
import org.openjdk.jmc.common.util.LabeledIdentifier;

public interface JfrEventVisitor {

  default void visitString(String eventType, String attribute, String value) {
  }

  default void visitNumber(String eventType, String attribute, long value) {
  }

  default void visitNumber(String eventType, String attribute, double value) {
  }

  default void visitBoolean(String eventType, String attribute, boolean value) {
  }

  default void visitMemory(IType<IItem> eventType, String attribute, long value, TypedUnit<?> unit) {
  }

  default void visitTimespan(String eventType, String attribute, long value, LinearUnit unit) {
  }

  default void visitTimestamp(String eventType, String attribute, long value, TimestampUnit unit) {
  }

  default void visitPercentage(String eventType, String attribute, double value) {
  }

  default void visitAddress(String eventType, String attribute, long value) {
  }

  default void visitFrequency(String eventType, String attribute, long value) {
  }

  default void visitLabeledIdentifier(String eventType, String attribute, LabeledIdentifier value) {
  }

  default void visitIMCClassLoader(String eventType, String attribute, IMCClassLoader value) {
  }

  default void visitIMCFrame(String eventType, String attribute, IMCFrame value) {
  }

  default void visitIMCMethod(String eventType, String attribute, IMCMethod value) {
  }

  default void visitIMCModule(String eventType, String attribute, IMCModule value) {
  }

  default void visitIMCOldObject(String eventType, String attribute, IMCOldObject value) {
  }

  default void visitIMCOldObjectArray(String eventType, String attribute, IMCOldObjectArray value) {
  }

  default void visitIMCOldObjectField(String eventType, String attribute, IMCOldObjectField value) {
  }

  default void visitIMCOldObjectGcRoot(String eventType, String attribute, IMCOldObjectGcRoot value) {
  }

  default void visitIMCPackage(String eventType, String attribute, IMCPackage value) {
  }

  default void visitIMCStackTrace(String eventType, String attribute, IMCStackTrace value) {
  }

  default void visitIMCThread(String eventType, String attribute, IMCThread value) {
  }

  default void visitIMCThreadGroup(String eventType, String attribute, IMCThreadGroup value) {
  }

  default void visitIMCType(String eventType, String attribute, IMCType value) {
  }
}
