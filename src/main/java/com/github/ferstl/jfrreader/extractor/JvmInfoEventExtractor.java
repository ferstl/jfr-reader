package com.github.ferstl.jfrreader.extractor;

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getEpochMillis;
import static com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor.getString;
import static java.util.Objects.requireNonNullElse;

public class JvmInfoEventExtractor {

  public String getJvmName(IItem item) {
    return getString(item, JdkAttributes.JVM_NAME);
  }

  public String getJvmVersion(IItem item) {
    return getString(item, JdkAttributes.JVM_VERSION);
  }

  public long getJvmStartTimeMs(IItem item) {
    return getEpochMillis(item, JdkAttributes.JVM_START_TIME);
  }

  public String getJvmArgs(IItem item) {
    return requireNonNullElse(getString(item, JdkAttributes.JVM_ARGUMENTS), "");
  }

  public String getJavaArgs(IItem item) {
    return requireNonNullElse(getString(item, JdkAttributes.JAVA_ARGUMENTS), "");
  }
}
