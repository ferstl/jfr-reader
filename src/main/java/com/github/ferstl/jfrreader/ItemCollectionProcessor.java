package com.github.ferstl.jfrreader;

import java.util.Optional;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IType;
import com.github.ferstl.jfrreader.extractor.ItemAttributeExtractor;

public class ItemCollectionProcessor {

  private final String applicationName;
  private final ItemProcessorRegistry itemProcessorRegistry;

  public ItemCollectionProcessor(String applicationName, ItemProcessorRegistry itemProcessorRegistry) {
    this.applicationName = applicationName;
    this.itemProcessorRegistry = itemProcessorRegistry;
  }

  public void processEvents(IItemCollection events) {
    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> type = eventTypeEntry.getType();
      Optional<ItemProcessor> processorOptional = this.itemProcessorRegistry.getItemProcessor(type.getIdentifier());
      if (processorOptional.isPresent()) {
        for (IItem item : eventTypeEntry) {
          ItemProcessor processor = processorOptional.get();
          long startTime = ItemAttributeExtractor.getEventStartTimeNs(item);
          processor.processEvent(startTime, item, this.applicationName);
        }
      }
    }
  }
}
