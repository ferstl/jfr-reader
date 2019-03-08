package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getDurationIn;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getLong;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getString;

public class GcPauseEventExtractor {

  public long getGcId(IItem item) {
    return getLong(item, JdkAttributes.GC_ID);
  }

  public String getGcName(IItem item) {
    return getString(item, JdkAttributes.GC_NAME);
  }

  public long getGcDurationUs(IItem item) {
    return getDurationIn(item, JfrAttributes.DURATION, UnitLookup.MICROSECOND);
  }
}
