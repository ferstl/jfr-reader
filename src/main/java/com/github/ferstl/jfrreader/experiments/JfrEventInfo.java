package com.github.ferstl.jfrreader.experiments;

import java.util.Objects;

public final class JfrEventInfo {

  private final String eventIdentifier;
  private final long startTimeEpochNanos;

  public JfrEventInfo(String eventIdentifier, long startTimeEpochNanos) {
    this.eventIdentifier = eventIdentifier;
    this.startTimeEpochNanos = startTimeEpochNanos;
  }

  public String getEventIdentifier() {
    return this.eventIdentifier;
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
        && Objects.equals(this.eventIdentifier, that.eventIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.startTimeEpochNanos, this.eventIdentifier);
  }
}
