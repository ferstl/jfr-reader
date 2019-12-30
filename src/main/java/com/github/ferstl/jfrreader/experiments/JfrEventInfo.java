package com.github.ferstl.jfrreader.experiments;

import java.util.Objects;

public final class JfrEventInfo {

  private final String eventIdentifier;
  private final String attributeIdentifier;
  private final long startTimeEpochNanos;

  public JfrEventInfo(String eventIdentifier, String attributeIdentifier, long startTimeEpochNanos) {
    this.eventIdentifier = eventIdentifier;
    this.attributeIdentifier = attributeIdentifier;
    this.startTimeEpochNanos = startTimeEpochNanos;
  }

  public String getEventIdentifier() {
    return this.eventIdentifier;
  }

  public String getAttributeIdentifier() {
    return this.attributeIdentifier;
  }

  public long getStartTimeEpochNanos() {
    return this.startTimeEpochNanos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (!(o instanceof JfrEventInfo)) { return false; }

    JfrEventInfo that = (JfrEventInfo) o;
    return this.startTimeEpochNanos == that.startTimeEpochNanos
        && Objects.equals(this.eventIdentifier, that.eventIdentifier)
        && Objects.equals(this.attributeIdentifier, that.attributeIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.startTimeEpochNanos, this.eventIdentifier, this.attributeIdentifier);
  }
}
