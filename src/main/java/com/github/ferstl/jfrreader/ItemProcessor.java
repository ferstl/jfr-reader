package com.github.ferstl.jfrreader;

import org.openjdk.jmc.common.item.IItem;

public interface ItemProcessor {

  void processEvent(long startTimeEpochNs, IItem item, String applicationName);
}
