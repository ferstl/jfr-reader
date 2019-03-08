package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getBoolean;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getLong;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getString;

public class GcConfigEventExtractor {

  public String getYoungCollector(IItem item) {
    return getString(item, JdkAttributes.YOUNG_COLLECTOR);
  }

  public String getOldCollector(IItem item) {
    return getString(item, JdkAttributes.OLD_COLLECTOR);
  }

  public long getNrOfParallelGcThreads(IItem item) {
    return getLong(item, JdkAttributes.PARALLEL_GC_THREADS);
  }

  public long getNrOfConcurrentGcThreads(IItem item) {
    return getLong(item, JdkAttributes.CONCURRENT_GC_THREADS);
  }

  public Boolean getExplicitGcEnabled(IItem item) {
    return getBoolean(item, JdkAttributes.EXPLICIT_GC_DISABLED);
  }

  public Boolean getExplicitGcConcurrent(IItem item) {
    return getBoolean(item, JdkAttributes.EXPLICIT_GC_CONCURRENT);
  }

  public long getGcTimeRatio(IItem item) {
    return getLong(item, JdkAttributes.GC_TIME_RATIO);
  }
}
