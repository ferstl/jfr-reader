package com.github.ferstl.jfrreader;

import java.util.Optional;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IType;
import com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor;

public class EventProcessor {

  private final EventRecorderRegistry eventRecorderRegistry;

  public EventProcessor(EventRecorderRegistry eventRecorderRegistry) {
    this.eventRecorderRegistry = eventRecorderRegistry;
  }

  public void processEvents(IItemCollection events) {
    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> type = eventTypeEntry.getType();
      Optional<EventRecorder> recorderOptional = this.eventRecorderRegistry.getRecorder(type.getIdentifier());
      if (recorderOptional.isPresent()) {
        for (IItem item : eventTypeEntry) {
          EventRecorder recorder = recorderOptional.get();
          long startTime = ItemAttributeExtractor.getEventStartTimeNs(item);
          recorder.recordEvent(startTime, item);
        }
      }
    }
  }
}
