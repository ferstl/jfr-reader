package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.LinearUnit;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;

// See fix-me comment in IItem
@SuppressWarnings("unchecked")
public class ItemAttributeExtractor {

  private ItemAttributeExtractor() {
    throw new AssertionError("Not instantiable");
  }

  public static long getEventStartTimeNs(IItem item) {
    return getEpochNanos(item, JfrAttributes.START_TIME);
  }

  static long getEpochNanos(IItem item, IAttribute<IQuantity> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .clampedLongValueIn(UnitLookup.EPOCH_NS);
  }

  static long getEpochMillis(IItem item, IAttribute<IQuantity> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .clampedLongValueIn(UnitLookup.EPOCH_MS);
  }

  static long getDurationIn(IItem item, IAttribute<IQuantity> duration, LinearUnit timeUnit) {
    return duration.getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .clampedLongValueIn(timeUnit);
  }

  static String getString(IItem item, IAttribute<String> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item);
  }

  static long getLong(IItem item, IAttribute<IQuantity> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .longValue();
  }

  static Boolean getBoolean(IItem item, IAttribute<Boolean> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item);
  }

  static long getBytes(IItem item, IAttribute<IQuantity> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .clampedLongValueIn(UnitLookup.BYTE);
  }

  static long getPercent(IItem item, IAttribute<IQuantity> attribute) {
    return attribute
        .getAccessor((IType<IItem>) item.getType())
        .getMember(item)
        .clampedLongValueIn(UnitLookup.PERCENT);
  }
}
