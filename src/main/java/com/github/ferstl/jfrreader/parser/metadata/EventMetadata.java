package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class EventMetadata extends ClassMetadata {

  public List<SettingValue> settings = new ArrayList<>();

  public EventMetadata(int id) {
    super(id);
  }
}
