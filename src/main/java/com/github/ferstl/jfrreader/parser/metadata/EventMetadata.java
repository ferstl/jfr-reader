package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;
import com.github.ferstl.jfrreader.parser.metadata.SettingValue.SettingValueBuilder;
import static java.util.stream.Collectors.toList;

public class EventMetadata extends ClassMetadata {

  private final List<SettingValue> settings;

  private EventMetadata(EventMetadataBuilder builder) {
    super(builder);
    this.settings = builder.settings.stream()
        .map(SettingValueBuilder::build)
        .collect(toList());
  }

  public static EventMetadataBuilder builder(int id) {
    return new EventMetadataBuilder(id);
  }

  public List<SettingValue> getSettings() {
    return this.settings;
  }

  public static class EventMetadataBuilder extends ClassMetadataBuilder {

    private final List<SettingValueBuilder> settings;
    private EventMetadata instance;

    public EventMetadataBuilder(int id) {
      super(id);
      this.settings = new ArrayList<>();
    }

    public EventMetadataBuilder setting(SettingValueBuilder setting) {
      this.settings.add(setting);
      return this;
    }

    @Override
    public EventMetadata build() {
      if (this.instance == null) {
        this.instance = new EventMetadata(this);
      }

      return this.instance;
    }
  }
}
