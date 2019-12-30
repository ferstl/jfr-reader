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

    JfrEventVisitor visitor = new JfrEventVisitorImpl();

    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> eventType = eventTypeEntry.getType();
      Map<IAccessorKey<?>, ? extends IDescribable> accessorKeys = eventType.getAccessorKeys();

      for (IItem eventItem : eventTypeEntry) {
        long startTimeEpochNanos = getStartTimeInEpochNanos(eventItem);
        JfrEventInfo eventInfo = new JfrEventInfo(eventType.getIdentifier(), startTimeEpochNanos);
        visitor.startEvent(eventInfo);

        for (IAccessorKey<?> accessorKey : accessorKeys.keySet()) {
          // Start time is always part of JfrEventInfo
          if (!accessorKey.equals(JfrAttributes.START_TIME)) {
            IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
            Object value = accessor.getMember(eventItem);

            processMember(visitor, eventInfo, accessorKey, value);
          }
        }

        visitor.endEvent(eventInfo);
      }

    }
  }

  @SuppressWarnings("unchecked")
  static long getStartTimeInEpochNanos(IItem eventItem) {
    return JfrAttributes.START_TIME.getAccessor((IType<IItem>) eventItem.getType())
        .getMember(eventItem)
        .clampedLongValueIn(UnitLookup.EPOCH_NS);
  }

  private static void processMember(JfrEventVisitor visitor, JfrEventInfo eventInfo, IAccessorKey<?> accessorKey, Object value) {
    if (value == null) {
      return;
    }

    String attributeIdentifier = accessorKey.getIdentifier();

    if (value instanceof String) {
      visitor.visitString(eventInfo, attributeIdentifier, (String) value);
    } else if (value instanceof Boolean) {
      visitor.visitBoolean(eventInfo, attributeIdentifier, (Boolean) value);
    } else if (value instanceof ITypedQuantity) {
      ContentType<?> contentType = accessorKey.getContentType();
      ITypedQuantity<?> castedValue = (ITypedQuantity<?>) value;

      if (contentType == UnitLookup.MEMORY) {
        visitor.visitMemory(eventInfo, attributeIdentifier, castedValue.longValue(), castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESPAN) {
        visitor.visitTimespan(eventInfo, attributeIdentifier, castedValue.longValue(), (LinearUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESTAMP) {
        visitor.visitTimestamp(eventInfo, attributeIdentifier, castedValue.longValue(), (TimestampUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.PERCENTAGE) {
        visitor.visitPercentage(eventInfo, attributeIdentifier, castedValue.doubleValue());
      } else if (contentType == UnitLookup.NUMBER) {
        if (LONG_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventInfo, attributeIdentifier, castedValue.longValue());
        } else if (DOUBLE_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventInfo, attributeIdentifier, castedValue.doubleValue());
        }
      } else if (contentType == UnitLookup.ADDRESS) {
        visitor.visitAddress(eventInfo, attributeIdentifier, castedValue.longValue());
      } else if (contentType == UnitLookup.FREQUENCY) {
        visitor.visitFrequency(eventInfo, attributeIdentifier, castedValue.longValue());
      }
    } else if (value instanceof LabeledIdentifier) {
      visitor.visitLabeledIdentifier(eventInfo, attributeIdentifier, (LabeledIdentifier) value);
    } else if (value instanceof IMCClassLoader) {
      visitor.visitIMCClassLoader(eventInfo, attributeIdentifier, (IMCClassLoader) value);
    } else if (value instanceof IMCFrame) {
      visitor.visitIMCFrame(eventInfo, attributeIdentifier, (IMCFrame) value);
    } else if (value instanceof IMCMethod) {
      visitor.visitIMCMethod(eventInfo, attributeIdentifier, (IMCMethod) value);
    } else if (value instanceof IMCModule) {
      visitor.visitIMCModule(eventInfo, attributeIdentifier, (IMCModule) value);
    } else if (value instanceof IMCOldObject) {
      visitor.visitIMCOldObject(eventInfo, attributeIdentifier, (IMCOldObject) value);
    } else if (value instanceof IMCOldObjectArray) {
      visitor.visitIMCOldObjectArray(eventInfo, attributeIdentifier, (IMCOldObjectArray) value);
    } else if (value instanceof IMCOldObjectField) {
      visitor.visitIMCOldObjectField(eventInfo, attributeIdentifier, (IMCOldObjectField) value);
    } else if (value instanceof IMCOldObjectGcRoot) {
      visitor.visitIMCOldObjectGcRoot(eventInfo, attributeIdentifier, (IMCOldObjectGcRoot) value);
    } else if (value instanceof IMCPackage) {
      visitor.visitIMCPackage(eventInfo, attributeIdentifier, (IMCPackage) value);
    } else if (value instanceof IMCStackTrace) {
      visitor.visitIMCStackTrace(eventInfo, attributeIdentifier, (IMCStackTrace) value);
    } else if (value instanceof IMCThread) {
      visitor.visitIMCThread(eventInfo, attributeIdentifier, (IMCThread) value);
    } else if (value instanceof IMCThreadGroup) {
      visitor.visitIMCThreadGroup(eventInfo, attributeIdentifier, (IMCThreadGroup) value);
    } else if (value instanceof IMCType) {
      visitor.visitIMCType(eventInfo, attributeIdentifier, (IMCType) value);
    } else {
      System.out.println("+".repeat(20) + " Unsupported Type:" + value.getClass() + "+".repeat(20));
    }
  }
}
