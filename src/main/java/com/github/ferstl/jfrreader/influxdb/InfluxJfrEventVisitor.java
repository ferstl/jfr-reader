package com.github.ferstl.jfrreader.influxdb;

import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
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
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.common.util.LabeledIdentifier;
import com.github.ferstl.jfrreader.experiments.JfrEventInfo;
import com.github.ferstl.jfrreader.experiments.JfrEventVisitor;
import static java.util.Objects.requireNonNullElse;

public class InfluxJfrEventVisitor implements JfrEventVisitor<Point.Builder> {

  private final InfluxDB influxDB;

  public InfluxJfrEventVisitor(InfluxDB influxDB) {
    this.influxDB = influxDB;
  }

  @Override
  public Point.Builder startEvent(JfrEventInfo eventInfo) {
    return Point.measurement(eventInfo.getEventIdentifier())
        .time(eventInfo.getStartTimeEpochNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public void endEvent(Builder context) {
    try {
      // there must be at least one field
      this.influxDB.write(context.build());
    } catch (IllegalArgumentException e) {
      //e.printStackTrace();
    }
  }

  @Override
  public void visitString(Builder context, String attribute, String value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitNumber(Builder context, String attribute, long value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitNumber(Builder context, String attribute, double value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitBoolean(Builder context, String attribute, boolean value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitMemory(Builder context, String attribute, long value, LinearUnit unit) {

  }

  @Override
  public void visitTimespan(Builder context, String attribute, long value, LinearUnit unit) {
    long nanoSeconds = unit.valueTransformTo(UnitLookup.NANOSECOND).targetValue(value);
    context.addField(attribute, nanoSeconds);
  }

  @Override
  public void visitTimestamp(Builder context, String attribute, long value, TimestampUnit unit) {
    long epochNanos = unit.valueTransformTo(UnitLookup.EPOCH_NS).targetValue(value);
    context.addField(attribute, epochNanos);
  }

  @Override
  public void visitPercentage(Builder context, String attribute, double value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitAddress(Builder context, String attribute, long value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitFrequency(Builder context, String attribute, long value) {
    context.addField(attribute, value);
  }

  @Override
  public void visitLabeledIdentifier(Builder context, String attribute, LabeledIdentifier value) {
    context.addField(attribute, value.getInterfaceId());
  }

  @Override
  public void visitIMCClassLoader(Builder context, String attribute, IMCClassLoader value) {
    context.addField(attribute + ".type", requireNonNullElse(value.getType().getFullName(), ""));
    context.addField(attribute + ".name", requireNonNullElse(value.getName(), ""));
  }

  @Override
  public void visitIMCFrame(Builder context, String attribute, IMCFrame value) {
    context.addField(attribute + ".type", value.getType().name());
    context.addField(attribute + ".byteCodeIndex", requireNonNullElse(value.getBCI(), -1));
    context.addField(attribute + ".lineNumber", requireNonNullElse(value.getFrameLineNumber(), -1));
    visitIMCMethod(context, attribute + ".method", value.getMethod());
  }

  @Override
  public void visitIMCMethod(Builder context, String attribute, IMCMethod value) {
    context.addField(attribute + ".type", value.getType().getFullName());
    context.addField(attribute + ".name", value.getMethodName());
    context.addField(attribute + ".descriptor", value.getFormalDescriptor());
    context.addField(attribute + ".modifier", value.getModifier());
    context.addField(attribute + ".native", value.isNative() != null ? value.isNative().toString() : "");
  }

  @Override
  public void visitIMCModule(Builder context, String attribute, IMCModule value) {
    context.addField(attribute + ".name", requireNonNullElse(value.getName(), ""));
    context.addField(attribute + ".version", requireNonNullElse(value.getVersion(), ""));
    context.addField(attribute + ".location", requireNonNullElse(value.getLocation(), ""));
    context.addField(attribute + ".classLoader", requireNonNullElse(value.getClassLoader().getName(), ""));
  }

  @Override
  public void visitIMCOldObject(Builder context, String attribute, IMCOldObject value) {
    context.addField(attribute + ".type", value.getType().getFullName());
    context.addField(attribute + ".address", value.getAddress().longValue());
  }

  @Override
  public void visitIMCOldObjectArray(Builder context, String attribute, IMCOldObjectArray value) {
    context.addField(attribute + ".size", value.getSize());
    context.addField(attribute + ".index", value.getIndex());
  }

  @Override
  public void visitIMCOldObjectField(Builder context, String attribute, IMCOldObjectField value) {
    context.addField(attribute + ".name", value.getName());
    context.addField(attribute + ".modifier", value.getModifier());
  }

  @Override
  public void visitIMCOldObjectGcRoot(Builder context, String attribute, IMCOldObjectGcRoot value) {
    context.addField(attribute + ".type", value.getType());
    context.addField(attribute + ".system", value.getSystem());
  }

  @Override
  public void visitIMCPackage(Builder context, String attribute, IMCPackage value) {
    String moduleName = "";
    if (value.getModule() != null && value.getModule().getName() != null) {
      moduleName = value.getModule().getName();
    }

    context.addField(attribute + ".name", value.getName());
    context.addField(attribute + ".module", moduleName);
    context.addField(attribute + ".exported", value.isExported());
  }

  @Override
  public void visitIMCStackTrace(Builder context, String attribute, IMCStackTrace value) {
  }

  @Override
  public void visitIMCThread(Builder context, String attribute, IMCThread value) {
    context.addField(attribute + ".threadName", value.getThreadName());
    context.addField(attribute + ".threadId", value.getThreadId().longValue());
    context.addField(attribute + ".threadGroup", value.getThreadGroup() != null ? value.getThreadGroup().getName() : "");
  }

  @Override
  public void visitIMCThreadGroup(Builder context, String attribute, IMCThreadGroup value) {
    context.addField(attribute + ".name", value.getName());
    context.addField(attribute + ".parent", value.getParent() != null ? value.getParent().getName() : "");
  }

  @Override
  public void visitIMCType(Builder context, String attribute, IMCType value) {
    context.addField(attribute, value.getFullName());
  }
}
