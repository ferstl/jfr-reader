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

public interface JfrEventVisitor<C> {

  default C startEvent(JfrEventInfo eventInfo) {
    return null;
  }

  default void endEvent(C context) {
  }

  default void visitString(C context, String attribute, String value) {
  }

  default void visitNumber(C context, String attribute, long value) {
  }

  default void visitNumber(C context, String attribute, double value) {
  }

  default void visitBoolean(C context, String attribute, boolean value) {
  }

  default void visitMemory(C context, String attribute, long value, TypedUnit<?> unit) {
  }

  default void visitTimespan(C context, String attribute, long value, LinearUnit unit) {
  }

  default void visitTimestamp(C context, String attribute, long value, TimestampUnit unit) {
  }

  default void visitPercentage(C context, String attribute, double value) {
  }

  default void visitAddress(C context, String attribute, long value) {
  }

  default void visitFrequency(C context, String attribute, long value) {
  }

  default void visitLabeledIdentifier(C context, String attribute, LabeledIdentifier value) {
  }

  default void visitIMCClassLoader(C context, String attribute, IMCClassLoader value) {
  }

  default void visitIMCFrame(C context, String attribute, IMCFrame value) {
  }

  default void visitIMCMethod(C context, String attribute, IMCMethod value) {
  }

  default void visitIMCModule(C context, String attribute, IMCModule value) {
  }

  default void visitIMCOldObject(C context, String attribute, IMCOldObject value) {
  }

  default void visitIMCOldObjectArray(C context, String attribute, IMCOldObjectArray value) {
  }

  default void visitIMCOldObjectField(C context, String attribute, IMCOldObjectField value) {
  }

  default void visitIMCOldObjectGcRoot(C context, String attribute, IMCOldObjectGcRoot value) {
  }

  default void visitIMCPackage(C context, String attribute, IMCPackage value) {
  }

  default void visitIMCStackTrace(C context, String attribute, IMCStackTrace value) {
  }

  default void visitIMCThread(C context, String attribute, IMCThread value) {
  }

  default void visitIMCThreadGroup(C context, String attribute, IMCThreadGroup value) {
  }

  default void visitIMCType(C context, String attribute, IMCType value) {
  }
}
