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
import org.openjdk.jmc.common.unit.ITypedQuantity;
import org.openjdk.jmc.common.unit.LinearUnit;
import org.openjdk.jmc.common.unit.TimestampUnit;
import org.openjdk.jmc.common.unit.TypedUnit;
import org.openjdk.jmc.common.util.LabeledIdentifier;

public class JfrEventVisitor {

  void visitString(String eventType, String attribute, String value) {

  }

  void visitNumber(String eventType, String attribute, long value) {
  }

  void visitNumber(String eventType, String attribute, double value) {
  }

  void visitBoolean(String eventType, String attribute, boolean value) {

  }

  void visitMemory(IType<IItem> eventType, String attribute, long value, TypedUnit<?> unit) {
  }

  void visitTimespan(String eventType, String attribute, long value, LinearUnit unit) {
  }

  void visitTimestamp(String eventType, String attribute, long value, TimestampUnit unit) {
  }

  void visitPercentage(String eventType, String attribute, double value) {
  }

  void visitAddress(String eventType, String attribute, long value) {
  }

  void visitFrequency(String eventType, String attribute, long value) {
  }

  void visitTypedQuantity(String eventType, String attribute, ITypedQuantity value) {

  }

  void visitLabeledIdentifier(String eventType, String attribute, LabeledIdentifier value) {

  }

  void visitIMCClassLoader(String eventType, String attribute, IMCClassLoader value) {

  }

  void visitIMCFrame(String eventType, String attribute, IMCFrame value) {

  }

  void visitIMCMethod(String eventType, String attribute, IMCMethod value) {

  }

  void visitIMCModule(String eventType, String attribute, IMCModule value) {

  }

  void visitIMCOldObject(String eventType, String attribute, IMCOldObject value) {

  }

  void visitIMCOldObjectArray(String eventType, String attribute, IMCOldObjectArray value) {

  }

  void visitIMCOldObjectField(String eventType, String attribute, IMCOldObjectField value) {

  }

  void visitIMCOldObjectGcRoot(String eventType, String attribute, IMCOldObjectGcRoot value) {

  }

  void visitIMCPackage(String eventType, String attribute, IMCPackage value) {

  }

  void visitIMCStackTrace(String eventType, String attribute, IMCStackTrace value) {

  }

  void visitIMCThread(String eventType, String attribute, IMCThread value) {

  }

  void visitIMCThreadGroup(String eventType, String attribute, IMCThreadGroup value) {

  }

  void visitIMCType(String eventType, String attribute, IMCType value) {

  }

}
