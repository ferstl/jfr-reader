package com.github.ferstl.jfrreader.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;

public class JfrGenericReader {

  private static final String LONG_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$LongStored";
  private static final String DOUBLE_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$DoubleStored";

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);
    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());
    System.out.println("Loaded recording: " + recording);

    // analysis...
    Map<String, ContentType<?>> accessorIdentifiers = new TreeMap<>();
    HashSet<Class<?>> types = new HashSet<>();


    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> eventType = eventTypeEntry.getType();

      String eventIdentifier = eventType.getIdentifier();
      Map<IAccessorKey<?>, ? extends IDescribable> accessorKeys = eventType.getAccessorKeys();

      for (IItem eventItem : eventTypeEntry) {
        for (IAccessorKey<?> accessorKey : accessorKeys.keySet()) {
          accessorIdentifiers.put(accessorKey.getIdentifier(), accessorKey.getContentType());

          IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
          Object value = accessor.getMember(eventItem);

          types.add(value != null ? value.getClass() : null);
          processMember(eventType, accessorKey, value);
        }
      }

    }

    System.out.println();
    System.out.println("All accessors");
    for (Entry<String, ContentType<?>> entry : accessorIdentifiers.entrySet()) {
      String identifier = entry.getKey();
      ContentType<?> contentType = entry.getValue();
      System.out.println(identifier + ": " + contentType);
    }

    System.out.println();
    System.out.println("All types");
    for (Class<?> type : types) {
      System.out.println(type);
    }
  }

  private static void processMember(IType<IItem> eventType, IAccessorKey<?> accessorKey, Object value) {
    JfrEventVisitor visitor = new JfrEventVisitor();
    if (value == null) {
      return;
    }

    String eventIdentifier = eventType.getIdentifier();
    String attributeIdentifier = accessorKey.getIdentifier();

    if (value instanceof String) {
      visitor.visitString(eventIdentifier, attributeIdentifier, (String) value);
    } else if (value instanceof Boolean) {
      visitor.visitBoolean(eventIdentifier, attributeIdentifier, (Boolean) value);
    } else if (value instanceof ITypedQuantity) {
      ContentType<?> contentType = accessorKey.getContentType();
      ITypedQuantity<?> castedValue = (ITypedQuantity<?>) value;

      if (contentType == UnitLookup.MEMORY) {
        visitor.visitMemory(eventType, attributeIdentifier, castedValue.longValue(), castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESPAN) {
        visitor.visitTimespan(eventIdentifier, attributeIdentifier, castedValue.longValue(), (LinearUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESTAMP) {
        visitor.visitTimestamp(eventIdentifier, attributeIdentifier, castedValue.longValue(), (TimestampUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.PERCENTAGE) {
        visitor.visitPercentage(eventIdentifier, attributeIdentifier, castedValue.doubleValue());
      } else if (contentType == UnitLookup.NUMBER) {
        if (LONG_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventIdentifier, attributeIdentifier, castedValue.longValue());
        } else if (DOUBLE_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(eventIdentifier, attributeIdentifier, castedValue.doubleValue());
        }
      } else if (contentType == UnitLookup.ADDRESS) {
        visitor.visitAddress(eventIdentifier, attributeIdentifier, castedValue.longValue());
      } else if (contentType == UnitLookup.FREQUENCY) {
        visitor.visitFrequency(eventIdentifier, attributeIdentifier, castedValue.longValue());
      }
    } else if (value instanceof LabeledIdentifier) {
      visitor.visitLabeledIdentifier(eventIdentifier, attributeIdentifier, (LabeledIdentifier) value);
    } else if (value instanceof IMCClassLoader) {
      visitor.visitIMCClassLoader(eventIdentifier, attributeIdentifier, (IMCClassLoader) value);
    } else if (value instanceof IMCFrame) {
      visitor.visitIMCFrame(eventIdentifier, attributeIdentifier, (IMCFrame) value);
    } else if (value instanceof IMCMethod) {
      visitor.visitIMCMethod(eventIdentifier, attributeIdentifier, (IMCMethod) value);
    } else if (value instanceof IMCModule) {
      visitor.visitIMCModule(eventIdentifier, attributeIdentifier, (IMCModule) value);
    } else if (value instanceof IMCOldObject) {
      visitor.visitIMCOldObject(eventIdentifier, attributeIdentifier, (IMCOldObject) value);
    } else if (value instanceof IMCOldObjectArray) {
      visitor.visitIMCOldObjectArray(eventIdentifier, attributeIdentifier, (IMCOldObjectArray) value);
    } else if (value instanceof IMCOldObjectField) {
      visitor.visitIMCOldObjectField(eventIdentifier, attributeIdentifier, (IMCOldObjectField) value);
    } else if (value instanceof IMCOldObjectGcRoot) {
      visitor.visitIMCOldObjectGcRoot(eventIdentifier, attributeIdentifier, (IMCOldObjectGcRoot) value);
    } else if (value instanceof IMCPackage) {
      visitor.visitIMCPackage(eventIdentifier, attributeIdentifier, (IMCPackage) value);
    } else if (value instanceof IMCStackTrace) {
      visitor.visitIMCStackTrace(eventIdentifier, attributeIdentifier, (IMCStackTrace) value);
    } else if (value instanceof IMCThread) {
      visitor.visitIMCThread(eventIdentifier, attributeIdentifier, (IMCThread) value);
    } else if (value instanceof IMCThreadGroup) {
      visitor.visitIMCThreadGroup(eventIdentifier, attributeIdentifier, (IMCThreadGroup) value);
    } else if (value instanceof IMCType) {
      visitor.visitIMCType(eventIdentifier, attributeIdentifier, (IMCType) value);
    } else {
      System.out.println("+".repeat(20) + " Unsupported Type:" + value.getClass() + "+".repeat(20));
    }
  }
}
