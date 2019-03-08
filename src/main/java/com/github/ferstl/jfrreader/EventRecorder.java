package com.github.ferstl.jfrreader;

import org.openjdk.jmc.common.item.IItem;

@FunctionalInterface
public interface EventRecorder {

  void recordEvent(long startTimeEpochNs, IItem item);
}
