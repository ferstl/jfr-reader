package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getPercent;

public class CpuLoadEventExtractor {

  public long getJvmUserPercent(IItem item) {
    return getPercent(item, JdkAttributes.JVM_USER);
  }

  public long getJvmSystemPercent(IItem item) {
    return getPercent(item, JdkAttributes.JVM_SYSTEM);
  }

  public long getMachineTotalPercent(IItem item) {
    return getPercent(item, JdkAttributes.MACHINE_TOTAL);
  }
}
