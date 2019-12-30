package com.github.ferstl.jfrreader.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.openjdk.jmc.common.IDescribable;
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
import org.openjdk.jmc.common.item.IAccessorKey;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.ContentType;
import org.openjdk.jmc.common.unit.ITypedQuantity;
import org.openjdk.jmc.common.unit.LinearUnit;
import org.openjdk.jmc.common.unit.TimestampUnit;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.common.util.LabeledIdentifier;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;

public class JfrGenericReader {

  private static final String LONG_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$LongStored";
  private static final String DOUBLE_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$DoubleStored";

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);
    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());
    System.out.println("Loaded recording: " + recording);

    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> eventType = eventTypeEntry.getType();
      Map<IAccessorKey<?>, ? extends IDescribable> accessorKeys = eventType.getAccessorKeys();

      for (IItem eventItem : eventTypeEntry) {
        long startTimeEpochNanos = getStartTimeInEpochNanos(eventItem);
        for (IAccessorKey<?> accessorKey : accessorKeys.keySet()) {
          // Start time is always part of JfrEventInfo
          if (!accessorKey.equals(JfrAttributes.START_TIME)) {
            IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
            Object value = accessor.getMember(eventItem);

            processMember(startTimeEpochNanos, eventType, accessorKey, value);
          }
        }
      }

    }
  }

  @SuppressWarnings("unchecked")
  static long getStartTimeInEpochNanos(IItem eventItem) {
    return JfrAttributes.START_TIME.getAccessor((IType<IItem>) eventItem.getType())
        .getMember(eventItem)
        .clampedLongValueIn(UnitLookup.EPOCH_NS);
  }

  private static void processMember(long startTimeEpochNanos, IType<IItem> eventType, IAccessorKey<?> accessorKey, Object value) {
    JfrEventVisitor visitor = new JfrEventVisitorImpl();
    if (value == null) {
      return;
    }

    JfrEventInfo eventInfo = new JfrEventInfo(eventType.getIdentifier(), accessorKey.getIdentifier(), startTimeEpochNanos);

    if (value instanceof String) {
      visitor.visitString(eventInfo, (String) value);
    } else if (value instanceof Boolean) {
      visitor.visitBoolean(eventInfo, (Boolean) value);
    } else if (value instanceof ITypedQuantity) {
      ContentType<?> contentType = accessorKey.getContentType();
      ITypedQuantity<?> castedValue = (ITypedQuantity<?>) value;

      if (contentType == UnitLookup.MEMORY) {
        visitor.visitMemory(eventInfo, castedValue.longValue(), castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESPAN) {
        visitor.visitTimespan(eventInfo, castedValue.longValue(), (LinearUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESTAMP) {
        visitor.visitTimestamp(eventInfo, castedValue.longValue(), (TimestampUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.PERCENTAGE) {
        visitor.visitPercentage(eventInfo, castedValue.doubleValue());
      } else if (contentType == UnitLookup.NUMBER) {
        if (LONG_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventInfo, castedValue.longValue());
        } else if (DOUBLE_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventInfo, castedValue.doubleValue());
        }
      } else if (contentType == UnitLookup.ADDRESS) {
        visitor.visitAddress(eventInfo, castedValue.longValue());
      } else if (contentType == UnitLookup.FREQUENCY) {
        visitor.visitFrequency(eventInfo, castedValue.longValue());
      }
    } else if (value instanceof LabeledIdentifier) {
      visitor.visitLabeledIdentifier(eventInfo, (LabeledIdentifier) value);
    } else if (value instanceof IMCClassLoader) {
      visitor.visitIMCClassLoader(eventInfo, (IMCClassLoader) value);
    } else if (value instanceof IMCFrame) {
      visitor.visitIMCFrame(eventInfo, (IMCFrame) value);
    } else if (value instanceof IMCMethod) {
      visitor.visitIMCMethod(eventInfo, (IMCMethod) value);
    } else if (value instanceof IMCModule) {
      visitor.visitIMCModule(eventInfo, (IMCModule) value);
    } else if (value instanceof IMCOldObject) {
      visitor.visitIMCOldObject(eventInfo, (IMCOldObject) value);
    } else if (value instanceof IMCOldObjectArray) {
      visitor.visitIMCOldObjectArray(eventInfo, (IMCOldObjectArray) value);
    } else if (value instanceof IMCOldObjectField) {
      visitor.visitIMCOldObjectField(eventInfo, (IMCOldObjectField) value);
    } else if (value instanceof IMCOldObjectGcRoot) {
      visitor.visitIMCOldObjectGcRoot(eventInfo, (IMCOldObjectGcRoot) value);
    } else if (value instanceof IMCPackage) {
      visitor.visitIMCPackage(eventInfo, (IMCPackage) value);
    } else if (value instanceof IMCStackTrace) {
      visitor.visitIMCStackTrace(eventInfo, (IMCStackTrace) value);
    } else if (value instanceof IMCThread) {
      visitor.visitIMCThread(eventInfo, (IMCThread) value);
    } else if (value instanceof IMCThreadGroup) {
      visitor.visitIMCThreadGroup(eventInfo, (IMCThreadGroup) value);
    } else if (value instanceof IMCType) {
      visitor.visitIMCType(eventInfo, (IMCType) value);
    } else {
      System.out.println("+".repeat(20) + " Unsupported Type:" + value.getClass() + "+".repeat(20));
    }
  }
}
