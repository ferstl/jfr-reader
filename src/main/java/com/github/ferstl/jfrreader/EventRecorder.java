package com.github.ferstl.jfrreader;

import org.openjdk.jmc.common.item.IItem;

public interface EventRecorder {

  void recordEvent(long startTimeEpochNs, IItem item);
}
