package com.github.ferstl.jfrreader;

import java.time.Instant;
import javax.management.openmbean.CompositeData;

public class RecordingInfo {

  private final long id;
  private final String name;
  private final String state;
  private final long size;
  private final long maxAge;
  private final long maxSize;
  private final Instant startTime;
  private final Instant stopTime;


  RecordingInfo(CompositeData cd) {
    this.id = (Long) cd.get("id");
    this.name = (String) cd.get("name");
    this.state = (String) cd.get("state");
    this.size = (long) cd.get("size");
    this.maxAge = (Long) cd.get("maxAge");
    this.maxSize = (Long) cd.get("maxSize");
    this.startTime = Instant.ofEpochMilli((Long) cd.get("startTime"));
    this.stopTime = Instant.ofEpochMilli((Long) cd.get("stopTime"));
  }

  public long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getState() {
    return this.state;
  }

  public long getSize() {
    return this.size;
  }

  public long getMaxAge() {
    return this.maxAge;
  }

  public long getMaxSize() {
    return this.maxSize;
  }

  public Instant getStartTime() {
    return this.startTime;
  }

  public Instant getStopTime() {
    return this.stopTime;
  }

  @Override
  public String toString() {
    return "RecordingInfo{" +
        "id=" + this.id +
        ", name='" + this.name + '\'' +
        ", state='" + this.state + '\'' +
        ", size=" + this.size +
        ", maxAge=" + this.maxAge +
        ", maxSize=" + this.maxSize +
        ", startTime=" + this.startTime +
        ", stopTime=" + this.stopTime +
        '}';
  }
}
