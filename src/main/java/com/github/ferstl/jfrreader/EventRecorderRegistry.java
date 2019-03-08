package com.github.ferstl.jfrreader;

import java.util.Map;
import java.util.Optional;

public class EventRecorderRegistry {

  private final Map<String, EventRecorder> recorders;

  public EventRecorderRegistry(Map<String, EventRecorder> recorders) {
    this.recorders = recorders;
  }

  public Optional<EventRecorder> getRecorder(String eventIdentifier) {
    return Optional.ofNullable(this.recorders.get(eventIdentifier));
  }
}
