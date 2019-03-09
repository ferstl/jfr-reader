package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getDouble;

public class CpuLoadEventExtractor {

  public double getJvmUserPercent(IItem item) {
    return getDouble(item, JdkAttributes.JVM_USER);
  }

  public double getJvmSystemPercent(IItem item) {
    return getDouble(item, JdkAttributes.JVM_SYSTEM);
  }

  public double getMachineTotalPercent(IItem item) {
    return getDouble(item, JdkAttributes.MACHINE_TOTAL);
  }
}
