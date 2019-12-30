package com.github.ferstl.jfrreader;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import com.github.ferstl.jfrreader.experiments.JfrEventInfo;
import com.github.ferstl.jfrreader.experiments.JfrEventVisitor;
import com.github.ferstl.jfrreader.experiments.JfrEventVisitorImpl;

public class JfrEventProcessor {

  private static final String LONG_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$LongStored";
  private static final String DOUBLE_STORED = "org.openjdk.jmc.common.unit.ScalarQuantity$DoubleStored";

  private final IItemCollection events;

  public JfrEventProcessor(IItemCollection events) {
    this.events = events;
  }

  public static void main(String[] args) {
    Path recording = Paths.get(args[0]);
    JfrEventProcessor reader = JfrEventProcessor.forRecording(recording);
    System.out.println("Loaded recording: " + recording);

    JfrEventVisitor<JfrEventInfo> visitor = new JfrEventVisitorImpl();
    reader.accept(visitor);
  }

  public static JfrEventProcessor forRecording(Path recording) {
    try {
      IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());
      return new JfrEventProcessor(events);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (CouldNotLoadRecordingException e) {
      throw new IllegalStateException(e);
    }
  }

  public final <T> void accept(JfrEventVisitor<T> visitor) {
    for (IItemIterable eventTypeEntry : this.events) {
      IType<IItem> eventType = eventTypeEntry.getType();
      Map<IAccessorKey<?>, ? extends IDescribable> accessorKeys = eventType.getAccessorKeys();

      for (IItem eventItem : eventTypeEntry) {
        long startTimeEpochNanos = getStartTimeInEpochNanos(eventItem);
        T context = visitor.startEvent(new JfrEventInfo(eventType.getIdentifier(), startTimeEpochNanos));

        for (IAccessorKey<?> accessorKey : accessorKeys.keySet()) {
          // Start time is always part of JfrEventInfo
          if (!accessorKey.equals(JfrAttributes.START_TIME)) {
            IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
            Object value = accessor.getMember(eventItem);

            processMember(visitor, context, accessorKey, value);
          }
        }

        visitor.endEvent(context);
      }
    }
  }

  @SuppressWarnings("unchecked")
  static long getStartTimeInEpochNanos(IItem eventItem) {
    return JfrAttributes.START_TIME.getAccessor((IType<IItem>) eventItem.getType())
        .getMember(eventItem)
        .clampedLongValueIn(UnitLookup.EPOCH_NS);
  }

  private static <T> void processMember(JfrEventVisitor<T> visitor, T context, IAccessorKey<?> accessorKey, Object value) {
    if (value == null) {
      return;
    }

    String attributeIdentifier = accessorKey.getIdentifier();

    if (value instanceof String) {
      visitor.visitString(context, attributeIdentifier, (String) value);
    } else if (value instanceof Boolean) {
      visitor.visitBoolean(context, attributeIdentifier, (Boolean) value);
    } else if (value instanceof ITypedQuantity) {
      ContentType<?> contentType = accessorKey.getContentType();
      ITypedQuantity<?> castedValue = (ITypedQuantity<?>) value;

      if (contentType == UnitLookup.MEMORY) {
        visitor.visitMemory(context, attributeIdentifier, castedValue.longValue(), castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESPAN) {
        visitor.visitTimespan(context, attributeIdentifier, castedValue.longValue(), (LinearUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.TIMESTAMP) {
        visitor.visitTimestamp(context, attributeIdentifier, castedValue.longValue(), (TimestampUnit) castedValue.getUnit());
      } else if (contentType == UnitLookup.PERCENTAGE) {
        visitor.visitPercentage(context, attributeIdentifier, castedValue.doubleValue());
      } else if (contentType == UnitLookup.NUMBER) {
        if (LONG_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(context, attributeIdentifier, castedValue.longValue());
        } else if (DOUBLE_STORED.equals(value.getClass().getName())) {
          visitor.visitNumber(context, attributeIdentifier, castedValue.doubleValue());
        }
      } else if (contentType == UnitLookup.ADDRESS) {
        visitor.visitAddress(context, attributeIdentifier, castedValue.longValue());
      } else if (contentType == UnitLookup.FREQUENCY) {
        visitor.visitFrequency(context, attributeIdentifier, castedValue.longValue());
      }
    } else if (value instanceof LabeledIdentifier) {
      visitor.visitLabeledIdentifier(context, attributeIdentifier, (LabeledIdentifier) value);
    } else if (value instanceof IMCClassLoader) {
      visitor.visitIMCClassLoader(context, attributeIdentifier, (IMCClassLoader) value);
    } else if (value instanceof IMCFrame) {
      visitor.visitIMCFrame(context, attributeIdentifier, (IMCFrame) value);
    } else if (value instanceof IMCMethod) {
      visitor.visitIMCMethod(context, attributeIdentifier, (IMCMethod) value);
    } else if (value instanceof IMCModule) {
      visitor.visitIMCModule(context, attributeIdentifier, (IMCModule) value);
    } else if (value instanceof IMCOldObject) {
      visitor.visitIMCOldObject(context, attributeIdentifier, (IMCOldObject) value);
    } else if (value instanceof IMCOldObjectArray) {
      visitor.visitIMCOldObjectArray(context, attributeIdentifier, (IMCOldObjectArray) value);
    } else if (value instanceof IMCOldObjectField) {
      visitor.visitIMCOldObjectField(context, attributeIdentifier, (IMCOldObjectField) value);
    } else if (value instanceof IMCOldObjectGcRoot) {
      visitor.visitIMCOldObjectGcRoot(context, attributeIdentifier, (IMCOldObjectGcRoot) value);
    } else if (value instanceof IMCPackage) {
      visitor.visitIMCPackage(context, attributeIdentifier, (IMCPackage) value);
    } else if (value instanceof IMCStackTrace) {
      visitor.visitIMCStackTrace(context, attributeIdentifier, (IMCStackTrace) value);
    } else if (value instanceof IMCThread) {
      visitor.visitIMCThread(context, attributeIdentifier, (IMCThread) value);
    } else if (value instanceof IMCThreadGroup) {
      visitor.visitIMCThreadGroup(context, attributeIdentifier, (IMCThreadGroup) value);
    } else if (value instanceof IMCType) {
      visitor.visitIMCType(context, attributeIdentifier, (IMCType) value);
    } else {
      System.out.println("+".repeat(20) + " Unsupported Type:" + value.getClass() + "+".repeat(20));
    }
  }
}
