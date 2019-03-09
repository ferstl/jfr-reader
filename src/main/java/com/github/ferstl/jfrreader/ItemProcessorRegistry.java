package com.github.ferstl.jfrreader;

import java.util.Map;
import java.util.Optional;

public class ItemProcessorRegistry {

  private final Map<String, ItemProcessor> itemProcessors;

  public ItemProcessorRegistry(Map<String, ItemProcessor> itemProcessors) {
    this.itemProcessors = itemProcessors;
  }

  public Optional<ItemProcessor> getItemProcessor(String eventIdentifier) {
    return Optional.ofNullable(this.itemProcessors.get(eventIdentifier));
  }
}
