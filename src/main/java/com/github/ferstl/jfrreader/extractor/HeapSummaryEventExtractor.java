package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getBytes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getLong;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getString;

public class HeapSummaryEventExtractor {

  public long getGcId(IItem item) {
    return getLong(item, JdkAttributes.GC_ID);
  }

  public String getWhen(IItem item) {
    return getString(item, JdkAttributes.GC_WHEN);
  }

  public long getCommittedHeapBytes(IItem item) {
    return getBytes(item, JdkAttributes.GC_HEAPSPACE_COMMITTED);
  }

  public long getReservedHeapBytes(IItem item) {
    return getBytes(item, JdkAttributes.GC_HEAPSPACE_RESERVED);
  }

  public long getUsedHeapBytes(IItem item) {
    return getBytes(item, JdkAttributes.HEAP_USED);
  }
}
